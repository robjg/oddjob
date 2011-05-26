package org.oddjob.sql;

public class SQLBuilder {

	public static final String LF_REGEX = "(\n)|(\r\n)";
	
	private final StringBuilder builder = new StringBuilder(); 
	
	public void append(String sql) {
		
		String noLFs = sql.replaceAll(LF_REGEX, " ");

		if (builder.length() > 0) {
			builder.append(' ');			
		}
		
		builder.append(noLFs.trim());
	}
	
	@Override
	public String toString() {
		return builder.toString();
	}
	
}
