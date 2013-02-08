package org.oddjob.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.runtime.ExpressionParser;
import org.oddjob.arooa.runtime.ParsedExpression;
import org.oddjob.beanbus.AbstractBusComponent;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusException;
import org.oddjob.sql.SQLJob.DelimiterType;

/**
 * Parses SQL from an InputStream into individual statements.
 * 
 * @author rob
 */
public class ScriptParser extends AbstractBusComponent<String>
implements ArooaSessionAware {
	
    /**
     * Keep the format of a SQL block.
     */
    private boolean keepFormat;

    /**
     * Should properties be expanded in SQL.
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

    /**
     * Session.
     */
	private ArooaSession session;
		
	/**
	 * Input for the SQL.
	 */
	private InputStream input;
	
	/**
	 * Stop flag.
	 */
	private volatile boolean stop;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	/**
	 * Getter for keepFormat.
	 * 
	 * @return keepFormat flag.
	 */
	public boolean isKeepFormat() {
		return keepFormat;
	}

	/**
	 * Setter for keepFormat
	 * 
	 * @param keepformat The keepFormat flag.
	 */
	public void setKeepFormat(boolean keepformat) {
		this.keepFormat = keepformat;
	}

	/**
	 * Getter for expandProperties.
	 * 
	 * @return The expandProperties flag.
	 */
	public boolean isExpandProperties() {
		return expandProperties;
	}

	/**
	 * Setter for exapndProperties.
	 * 
	 * @param expandProperties The expandProperties flag.
	 */
	public void setExpandProperties(boolean expandProperties) {
		this.expandProperties = expandProperties;
	}

	/**
	 * Getter for delimiterType.
	 * 
	 * @return The delimiterType.
	 */
	public DelimiterType getDelimiterType() {
		return delimiterType;
	}

	/**
	 * Setter for delimiterType.
	 * 
	 * @param delimiterType The delimiterType.
	 */
	public void setDelimiterType(DelimiterType delimiterType) {
		this.delimiterType = delimiterType;
	}

	/**
	 * Getter for delimiter.
	 * 
	 * @return The delimiter.
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Setter for delimiter.
	 * 
	 * @param delimiter The delimiter.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Getter for encoding.
	 * 
	 * @return The encoding.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Setter for encoding.
	 * 
	 * @param encoding The encoding.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void go() throws BusException, IOException {

		stop = false;
		
		startBus();
		
		try {
			doProcessing();
			
	        stopBus();
		}
		finally {
			input.close();
		}
	}
	
	protected void doProcessing() throws BusException {
		
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
			throw new BusCrashException(e1);
		}

        while (!stop) {
            String line;
			try {
				line = in.readLine();
			} catch (IOException e) {
				throw new BusCrashException(e);
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
					throw new IllegalArgumentException(line, e);
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

	@Override
	protected void requestStopBus() throws BusCrashException {
		stop();
	}
	
	/**
	 * @param sql
	 * 
	 * @throws BusCrashException
	 */
	private void dispatch(String sql) throws BusCrashException {
		accept(sql);
	}
	
	String replaceProperties(String line) throws ArooaConversionException {
		ExpressionParser lineParser = session.getTools().getExpressionParser();
		ParsedExpression parsed = lineParser.parse(line);
		return parsed.evaluate(session, String.class);
	}
		
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
