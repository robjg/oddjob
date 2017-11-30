/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.schedules.IntervalSchedule;
import org.oddjob.scheduling.Keeper;
import org.oddjob.scheduling.LoosingOutcome;
import org.oddjob.scheduling.Outcome;
import org.oddjob.scheduling.WinningOutcome;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateListener;

/**
 * @oddjob.description Provides a {@link Keeper} that uses a database 
 * table.
 * <p>
 * The keeper uses a simple 'first to insert' a row wins methodology for deciding
 * winner and looser. This is quite primitive and decides that any exception 
 * from the insert operation is a duplicate key exception and therefore a 
 * looser.
 * <p>
 * A {@link LoosingOutcome} will continue to Poll the database (for as long
 * as it has listeners) until the work is complete. The default polling schedule
 * polls every 5 seconds indefinitely. The <code>pollSchedule</code> property
 * can be used to poll for a limited time, after which it flags an exception
 * state. This could be used by loosing servers to flag the winner is taking
 * too long and has possibly crashed.
 * <p>
 * This is an example of the SQL that would create a suitable table.
 * 
 * <pre><code>
 * CREATE TABLE oddjob_grabbable(
 *   key VARCHAR(32),
 *   instance VARCHAR(32),
 *   winner VARCHAR(32),
 *   complete boolean,
 *  CONSTRAINT oddjob_pk PRIMARY KEY (key, instance))
 * </pre></code>
 * 
 * <p>
 * This service does not tidy up the database so rows will grow indefinitely.
 * A separate tidy job should be implemented.
 * 
 * @oddjob.example
 * 
 * See the User Guide.
 * 
 * @author Rob Gordon.
 */
public class SQLKeeperService {
	private static final Logger logger = LoggerFactory.getLogger(SQLKeeperService.class);

	/** The default table name. */
	public static final String TABLE_NAME = "oddjob_grabbable";
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The name.
	 * @oddjob.required No. 
	 */
	private String name;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The {@link ConnectionType} to use.
	 * @oddjob.required Yes. 
	 */
	private Connection connection;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The database table name.
	 * @oddjob.required No. 
	 */
	private String table;
	
	/** 
	 * @oddjob.property scheduleExecutorService
	 * @oddjob.description The scheduling service for polling.
	 * @oddjob.required No - provided by Oddjob. 
	 */
	private ScheduledExecutorService scheduler;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The schedule to provide the polling interval.
	 * @oddjob.required No - defaults to a 5 second {@link IntervalSchedule}. 
	 */
	private Schedule pollSchedule;
	
	/** Flag to indicate service is running. */
	private volatile boolean running;
	
	/** Keep track of polling loosers so they may be stopped from polling. */
	private final List<ALoosingOutcome> loosers = 
		new ArrayList<ALoosingOutcome>();
	
	/**
	 * Set up a default poll schedule.
	 */
	{
		pollSchedule = new IntervalSchedule(5000L);
	}
	
	/**
	 * Start the service.
	 * 
	 * @throws SQLException
	 */
	public void start() throws SQLException {
		
		if (connection == null) {
			throw new NullPointerException("No Connection.");
		}
		
		if (table == null) {
			table = TABLE_NAME;
		}		
		
		running = true;
	}
	
	/**
	 * Stop the service.
	 * 
	 * @throws SQLException
	 */
	public void stop() throws SQLException {
		
		running = false;
		
		while (true){
			ALoosingOutcome looser = null;
			synchronized (loosers) {
				if (loosers.size() > 0) {
					looser = loosers.remove(0);
				}
			}
			if (looser == null) {
				break;
			}
			looser.stop();
		}
		
		if (connection != null) {
			connection.close();
		}		
	}
	
	/**
	 * Provide a {@link Keeper}.
	 * 
	 * @param keeperKey The keepers key. Must not be null.
	 * 
	 * @return A keeper. Never null.
	 */
	public Keeper getKeeper(final String keeperKey) {
		
		if (keeperKey == null) {
			throw new NullPointerException("No Identifier.");
		}
		
		return new Keeper() {
			
			@Override
			public Outcome grab(String ourIdentifier, Object instanceIdentifier) {

				if (!running) {
					throw new IllegalStateException(
							"SQLKeeperService not Running.");
				}
				if (ourIdentifier == null) {
					throw new NullPointerException(
							"The Grabber Identifier must not be null.");
				}
				if (instanceIdentifier == null) {
					throw new NullPointerException(
							"The Instance Identifier must not be null.");
				}
				
				try {
					PreparedStatement insertStmt = createInsertStatementFor(
							connection, keeperKey, 
							ourIdentifier, instanceIdentifier);
					try {
						insertStmt.execute();

						// No exception? We're the winner.
						logger.info(ourIdentifier + " won grab for " + 
								instanceIdentifier);
						return new AWinningOutcome(ourIdentifier, 
								keeperKey, instanceIdentifier);
					}
					catch (SQLException e) {
						logger.info(ourIdentifier + " lost grab for " + 
								instanceIdentifier);
						logger.debug("Lost with exception: " + e.toString());
					}
					finally {
						insertStmt.close();				
					}

					Query query = new Query(keeperKey, instanceIdentifier);
					query.query();

					if (ourIdentifier.equals(query.getWinner())
							&& !query.isComplete()) {
						// Winner must be restarting
						return new AWinningOutcome(ourIdentifier, 
								keeperKey, instanceIdentifier);
					}
					// we could be the last winner and we completed which means
					// we're restarting without persistence. So return a complete looser
					// to avoid re-running.

					ALoosingOutcome looser = new ALoosingOutcome(
							query.getWinner(), keeperKey, instanceIdentifier);
					
					return looser;
				}
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public String toString() {
				return "The Keeper: " + keeperKey;
			}
		};
	}	
	
	/**
	 * Winning Outcome.
	 */
	class AWinningOutcome implements WinningOutcome {
		
		private final String winner;
		
		private final String keeperKey;
		private final Object instanceIdentifier;
		
		public AWinningOutcome(String winner, 
				String keeperKey, Object instanceIdentifier) {
			this.winner = winner;
			this.keeperKey = keeperKey;
			this.instanceIdentifier = instanceIdentifier;
		}
		
		@Override
		public boolean isWon() {
			return true;
		}
		
		@Override
		public String getWinner() {
			return winner;
		}
		
		@Override
		public void complete() {
			try {
				PreparedStatement updateStmt = createUpdateStatementFor(
						connection, keeperKey, instanceIdentifier);
				
				int count = updateStmt.executeUpdate();
				logger.info("Set complete keeper complete, update count [" +
						count + "]");
				updateStmt.close();
			} 
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Encapsulates querying the database for the winner is complete.
	 * @author rob
	 *
	 */
	class Query {
		
		private boolean complete;
		private String winner;
		
		private final String keeperKey;
		private final Object instanceIdentifier;
		
		public Query(String keeperKey, Object instanceIdentifier) {
			this.keeperKey = keeperKey;
			this.instanceIdentifier = instanceIdentifier;
		}
		
		void query() throws SQLException {
			
			PreparedStatement queryStmt = createQueryStatementFor(
					connection, keeperKey, instanceIdentifier);
			
			ResultSet rs = queryStmt.executeQuery();
			
			if (!rs.next()) {
				throw new IllegalStateException(
						"No row for " + keeperKey + ", " +
						instanceIdentifier);
			}
			
			winner = rs.getString(1);
			complete = rs.getBoolean(2);
			queryStmt.close();
		}
		
		public String getWinner() {
			return winner;
		}
		
		public boolean isComplete() {
			return complete;
		}
	}

	/**
	 * Encapsulate Polling.
	 */
	class Poll implements Runnable {

		private final ALoosingOutcome loosing;
		
		private final String keeperKey;
		
		private final Object instanceIdentifier;
		
		private volatile Future<?> future;
		
		private volatile ScheduleContext scheduleContext =
			new ScheduleContext(new Date());
		
		public Poll(ALoosingOutcome loosing, 
				String keeperKey,
				Object instanceIdentifier) {
			this.loosing = loosing;
			this.keeperKey = keeperKey;
			this.instanceIdentifier = instanceIdentifier;
		}
		
		@Override
		public void run() {
			synchronized (loosers) {
				loosers.remove(loosing);
			}
			
			try {
				Query query = new Query(keeperKey, instanceIdentifier);
				query.query();
				
				if (query.isComplete()) {
					loosing.stateHandler.waitToWhen(new IsAnyState(), 
							new Runnable() {
						public void run() {
							loosing.stateHandler.setState(JobState.COMPLETE);
							loosing.stateHandler.fireEvent();
						}
					});
				}
				else {
					ScheduleResult nextDue = pollSchedule.nextDue(scheduleContext);
					if (nextDue == null) {
						loosing.stateHandler.waitToWhen(new IsAnyState(), 
								new Runnable() {
							public void run() {
								loosing.stateHandler.setStateException(
										JobState.EXCEPTION,
										new Exception("Job failed to complete " +
												"in expected time."));
								loosing.stateHandler.fireEvent();
							}
						});
					}
					else {
						scheduleContext = scheduleContext.move(
								new IntervalTo(nextDue).getToDate());
						long delay = nextDue.getToDate().getTime() - 
							new Date().getTime();
						if (delay <= 0) {
							delay = 0;
						}
						synchronized (loosers) {
							if (running) {
								this.future = 
										scheduler.schedule(this, delay, 
												TimeUnit.MILLISECONDS);
								loosers.add(loosing);
							}
						}
					}
				}
			}
			catch (SQLException e) {
				logger.error("Failed to Poll Keeper.", e);
			}
		}		
		
		private void stop() {
			if (future != null) {
				future.cancel(false);
				future = null;
			}
		}

	}
	
	/**
	 * A Loosing Outcome.
	 *
	 */
	class ALoosingOutcome implements LoosingOutcome {
		
		private final String winner;
		
		private final JobStateHandler stateHandler = 
			new JobStateHandler(this);

		private final Poll poll;
		
		public ALoosingOutcome(String winner, String keeperKey,
				Object instanceIdentifier) {
			
			this.winner = winner;
			
			poll = new Poll(this, keeperKey, instanceIdentifier);
		}
		
		@Override
		public void addStateListener(StateListener listener) {
			if (stateHandler.listenerCount() == 0) {
				stateHandler.waitToWhen(new IsAnyState(), 
						new Runnable() {
					@Override
					public void run() {
						stateHandler.setState(JobState.EXECUTING);
					}
				});
				poll.run();
			}
			stateHandler.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			stateHandler.removeStateListener(listener);
			if (stateHandler.listenerCount() == 0) {
				synchronized (loosers) {
					loosers.remove(this);
				}
				poll.stop();
			}
		}
		
		@Override
		public StateEvent lastStateEvent() {
			return stateHandler.lastStateEvent();
		}
		
		private void stop() {
			poll.stop();
			stateHandler.waitToWhen(new IsStoppable(), 
					new Runnable() {
				public void run() {
					stateHandler.setState(JobState.INCOMPLETE);
					stateHandler.fireEvent();
				}
			});				
		}
		
		@Override
		public String getWinner() {
			return winner;
		}
		
		@Override
		public boolean isWon() {
			return false;
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the connection.
	 * 
	 * @param connection The connection.
	 */
	public void setConnection(Connection connection) throws SQLException {
		this.connection = connection;
	}

	/**
	 * @oddjob.property pollerCount
	 * @oddjob.description The number of outstanding loosing outcome's 
	 * polling of the database that are still in progress.
	 * 
	 * @return The number.
	 */
	public int getPollerCount() {
		return loosers.size();
	}
	
	/**
	 * Provide a PreparedStatement for the insert operation.
	 * 
	 * @param connection
	 * @param keeperKey
	 * @param ourIdentifier
	 * @param instanceIdentifier
	 * 
	 * @return
	 * 
	 * @throws SQLException
	 */
	protected PreparedStatement createInsertStatementFor(
			Connection connection, String keeperKey, 
			String ourIdentifier, Object instanceIdentifier) 
	throws SQLException {
		
		PreparedStatement insertStmt = connection.prepareStatement(
				"insert into " + getTable() + " (key, instance, winner) " +
				" values (?, ?, ?)");
		
		insertStmt.setString(1, keeperKey);
		insertStmt.setObject(2, instanceIdentifier);
		insertStmt.setString(3, ourIdentifier);

		return insertStmt;
	}
	
	/**
	 * Create the PreparedStatement for the query of who won
	 * and is work complete yet.
	 * 
	 * @param connection
	 * @param keeperKey
	 * @param instanceIdentifier
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createQueryStatementFor(Connection connection, 
			String keeperKey, Object instanceIdentifier) 
	throws SQLException {
		
		PreparedStatement queryStmt = connection.prepareStatement(
				"select winner, complete from " + getTable() + 
					" where key = ? and instance = ?");
		
		queryStmt.setString(1, keeperKey);
		queryStmt.setObject(2, instanceIdentifier);
		
		return queryStmt;
	}
	
	/**
	 * Create the PreparedStatemenet for updating won work is
	 * complete.
	 * 
	 * @param connection
	 * @param keeperKey
	 * @param instanceIdentifier
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createUpdateStatementFor(Connection connection, 
			String keeperKey, Object instanceIdentifier) 
	throws SQLException {
		
		PreparedStatement updateStmt = connection.prepareStatement(
				"update " + getTable() + 
				" set complete = true where key = ? and instance = ?");
		updateStmt.setString(1, keeperKey);		
		updateStmt.setObject(2, instanceIdentifier);		
		
		return updateStmt;
	}
	
	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Schedule getPollSchedule() {
		return pollSchedule;
	}

	public void setPollSchedule(Schedule schedule) {
		this.pollSchedule = schedule;
	}

	@Inject
	public void setScheduleExecutorService(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}
}
