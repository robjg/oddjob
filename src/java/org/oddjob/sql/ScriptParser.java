package org.oddjob.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.runtime.ExpressionParser;
import org.oddjob.arooa.runtime.ParsedExpression;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BusAware;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusException;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.CrashBusException;
import org.oddjob.beanbus.Destination;
import org.oddjob.beanbus.Driver;
import org.oddjob.beanbus.StageListener;
import org.oddjob.beanbus.StageNotifier;
import org.oddjob.beanbus.StageSupport;
import org.oddjob.sql.SQLJob.DelimiterType;

public class ScriptParser implements ArooaSessionAware, 
Driver<String>, BusAware, StageNotifier {
	
	private static final Logger logger = Logger.getLogger(ScriptParser.class);
	
    /**
     * Keep the format of a sql block?
     */
    private boolean keepFormat;

    /**
     * should properties be expanded in text?
     * false for backwards compatibility
     *
     * @since Ant 1.7
     */
    private boolean expandProperties;

    /**
     * The delimiter type indicating whether the delimiter will
     * only be recognized on a line by itself
     */
    private DelimiterType delimiterType = DelimiterType.NORMAL;

    /**
     * SQL Statement delimiter
     */
    private String delimiter = ";";

    /**
     * Encoding to use when reading SQL statements from a file
     */
    private String encoding = null;

	private ArooaSession session;
		
	private InputStream input;
	
	private Destination<? super String> to;
	
	private final StageSupport batchSupport = new StageSupport(this);
	
	private volatile boolean stop;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	public boolean isKeepFormat() {
		return keepFormat;
	}

	public void setKeepFormat(boolean keepformat) {
		this.keepFormat = keepformat;
	}



	public boolean isExpandProperties() {
		return expandProperties;
	}

	public void setExpandProperties(boolean expandProperties) {
		this.expandProperties = expandProperties;
	}

	public DelimiterType getDelimiterType() {
		return delimiterType;
	}

	public void setDelimiterType(DelimiterType delimiterType) {
		this.delimiterType = delimiterType;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public void go() throws BusException {

		stop = false;
		
        StringBuffer sql = new StringBuffer();
        
        BufferedReader in = null;
        
        try {
        	if (encoding == null) {
    			in = new BufferedReader(
    					new InputStreamReader(input));
        	}
        	else {
    			in = new BufferedReader(
    					new InputStreamReader(input, encoding));
        	}
		} catch (UnsupportedEncodingException e1) {
			throw new CrashBusException(e1);
		}

        while (!stop) {
            String line;
			try {
				line = in.readLine();
			} catch (IOException e) {
				throw new CrashBusException(e);
			}
            if (line == null) {
            	break;
            }
        	
            if (!keepFormat) {
                line = line.trim();
            }
            if (expandProperties) {
                try {
					line = replaceProperties(line);
				} catch (ArooaConversionException e) {
					throw new BadBeanException(line, e);
				}
            }
            if (!keepFormat) {
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if ("REM".equalsIgnoreCase(token)) {
                        continue;
                    }
                }
            }

            sql.append(keepFormat ? "\n" : " ").append(line);

            // SQL defines "--" as a comment to EOL
            // and in Oracle it may contain a hint
            // so we cannot just remove it, instead we must end it
            if (!keepFormat && line.indexOf("--") >= 0) {
                sql.append("\n");
            }
            if ((delimiterType.equals(DelimiterType.NORMAL) 
            		&& line.lastIndexOf(delimiter) == line.length() - delimiter.length())
                    || (delimiterType.equals(DelimiterType.ROW) && line.equalsIgnoreCase(delimiter))) {
            	String text = sql.substring(
            			0, sql.length() - delimiter.length()).trim();
            	if (text.length() > 0) {
                    dispatch(text);
            	}
                sql.replace(0, sql.length(), "");
            }
        }
        
        // Catch any statements not followed by ;
        String text = sql.toString().trim();
        if (text.length() > 0) {
            dispatch(text.toString());
        }
	}

	private void dispatch(String sql) throws BadBeanException, CrashBusException {
		batchSupport.fireBatchStarting("Sql Statement", sql);
		to.accept(sql);
		batchSupport.fireBatchComplete();		
	}
	
	String replaceProperties(String line) throws ArooaConversionException {
		ExpressionParser lineParser = session.getTools().getExpressionParser();
		ParsedExpression parsed = lineParser.parse(line);
		return parsed.evaluate(session, String.class);
	}
	
	@Override
	public void setBus(BeanBus bus) {
		bus.addBusListener(new BusListener() {
			
			@Override
			public void busTerminated(BusEvent event) {
			}
			
			@Override
			public void busStopping(BusEvent event) throws CrashBusException {
				event.getSource().removeBusListener(this);
				try {
					input.close();
				} catch (IOException e) {
					throw new CrashBusException(e);
				}
			}
			
			@Override
			public void busStarting(BusEvent event) throws CrashBusException {
			}
			
			@Override
			public void busCrashed(BusEvent event, BusException e) {
				event.getSource().removeBusListener(this);
				try {
					input.close();
				} catch (IOException ioe) {
					logger.error(ioe);
				}
				
			}
		});
		if (to instanceof BusAware) {
			((BusAware) to).setBus(bus);
		}
	}
	
	@Override
	public void addStageListener(StageListener listener) {
		batchSupport.addStageListener(listener);
	}
	
	@Override
	public void removeStageListener(StageListener listener) {
		batchSupport.removeStageListener(listener);
	}

	@Override
	public void setTo(Destination<? super String> to) {
		this.to = to;
	}
	
	@Override
	public void stop() {
		stop = true;
	}

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}
}
