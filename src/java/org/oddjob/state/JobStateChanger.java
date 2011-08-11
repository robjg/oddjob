package org.oddjob.state;

import org.oddjob.images.IconHelper;
import org.oddjob.persist.Persistable;

/**
 * A {@link StateChanger} for {@link JobState}s.
 * 
 * @author rob
 *
 */
public class JobStateChanger extends BaseStateChanger<JobState> {
		
	public JobStateChanger(JobStateHandler stateHandler,
			IconHelper iconHelper, Persistable persistable) {
		super(stateHandler, iconHelper, persistable, JobState.EXCEPTION);
	}
	
}
