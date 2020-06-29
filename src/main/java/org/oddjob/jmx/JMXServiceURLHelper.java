package org.oddjob.jmx;

import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a JMX Service URL to see if it is a full URL or just the
 * host name and optionally port and optionally path identifier.
 * 
 * @author rob
 *
 */
public class JMXServiceURLHelper {

	private static final String URL_START = "service:jmx";
	
	private static final Pattern URL_END = Pattern.compile(
			"(.+?)(:\\d+)?(/.+)?");
	
	public JMXServiceURL parse(String url) throws MalformedURLException {
		
		if (url.startsWith(URL_START)) {
			return new JMXServiceURL(url);
		}
		
		Matcher matcher = URL_END.matcher(url);

		if (!matcher.matches()) {
			throw new MalformedURLException("Can't create a URL with [" + 
					url + "]");
		}
		
		String hostName = matcher.group(1);
		String port = matcher.group(2);
		String lastBit = matcher.group(3);
		
		if (port == null) {
			port = "";
		}
		
		if (lastBit == null) {
			lastBit = "/jmxrmi";
		}
		
		String path = "/jndi/rmi://" + hostName + port + lastBit;
		
		return new JMXServiceURL("rmi", "", 0, path);
	}
	
}
