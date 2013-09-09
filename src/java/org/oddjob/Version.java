package org.oddjob;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oddjob.arooa.ArooaConstants;
import org.oddjob.arooa.utils.DateHelper;

/**
 * Oddjob's version. Comparison and Equality are only based on the 
 * major minor and patch versions, not on the snapshot or date properties.
 * 
 * @author rob
 *
 */
public class Version implements Comparable<Version>{

    /** Oddjob version. Set by Ant during build. */
	private static final String VERSION = "@version@";
	
    /** Build date. Set by Ant during build. */
	private static final String DATE = "@date@";
	
	private static final Version current;
	
	static {
		Version version = versionFor(VERSION, DATE);
		if (version == null) {
			current = new Version(0, 0, 0);
		}
		else {
			current = version;
		}
	}
	
	private final int major;
	
	private final int minor;
	
	private final int patch;
	
	private final boolean snapshot;
	
	private final Date buildDate;
	
	private final String versionAsText;
	
	/**
	 * Create a new instance. The snapshot property will be false and
	 * the date property will be null. 
	 * 
	 * @param major
	 * @param minor
	 * @param patch
	 */
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, false, null);
	}
	
	/**
	 * Create a new instance.
	 * 
	 * @param major
	 * @param minor
	 * @param patch
	 * @param snapshot
	 * @param buildDate
	 */
	public Version(int major, int minor, int patch,
			boolean snapshot, Date buildDate) {
		
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.snapshot = snapshot;
		this.buildDate = buildDate;
		
		this.versionAsText =  "" + major + "." + minor + "." + patch + 
			(snapshot ? "-SNAPSHOT" : "") +
			(buildDate == null ? "" : " " + 
					DateHelper.formatDateTimeInteligently(buildDate));
	}
	
	/**
	 * Get the major version.
	 * 
	 * @return
	 */
	public int getMajor() {
		return major;
	}
	
	/**
	 * Get the minor version.
	 * 
	 * @return
	 */
	public int getMinor() {
		return minor;
	}
	
	/**
	 * Get the patch version.
	 * @return
	 */
	public int getPatch() {
		return patch;
	}
	
	/**
	 * Is this build version representing a snapshot version.
	 * 
	 * @return true if this version is a snapshot, otherwise false.
	 */
	public boolean isSnapshot() {
		return snapshot;
	}
	
	/**
	 * Get the build date and time if one was provided.
	 * 
	 * @return A date or null.
	 */
	public Date getBuildDate() {
		return buildDate;
	}
	
	@Override
	public int compareTo(Version o) {
		int comparison = new Integer(this.major).compareTo(
				new Integer(o.major));
		if (comparison == 0) {
			return 0;
		}
		comparison = new Integer(this.minor).compareTo(
				new Integer(o.minor));
		if (comparison == 0) {
			return 0;
		}

		return new Integer(this.patch).compareTo(
				new Integer(o.patch));
	}
	
	@Override
	public int hashCode() {
		return major * 31 + minor * 31 + patch * 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Version) {
			return ((Version) obj).compareTo(this) == 0;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return versionAsText;
	}
	
	/**
	 * Get the current version of this build. The current version might be 
	 * 0.0.0-SNAPSHOT if the version text couldn't be parsed - i.e. it wasn't
	 * built with ant.
	 * 
	 * @return A version. Never null.
	 */
	public static Version current() {
		return current;
	}
	
	/**
	 * Create a version for the given version text and date text.
	 * <p>
	 * The version is expected to be in the form <code>major.minor.patch</code>
	 * or <code>major.minor.patch-SNAPSHOT</code>. If the version is not
	 * in this form null will be returned.
	 * <p>
	 * The date is expected to be one of the standard date time formats
	 * as specified in {@link ArooaConstants}. If it is not in this format
	 * a version with a null date property will be returned.
	 * 
	 * @param version The version as text. Must not be null.
	 * @param date The date as text. May be null.

	 * @return A version or null.
	 */
	public static Version versionFor(String version, String date) {
		
		Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-SNAPSHOT)?");
		
		Matcher matcher = pattern.matcher(version);
		
		if (!matcher.matches()) {
			return null;
		}
		
		int major = Integer.parseInt(matcher.group(1));
		int minor = Integer.parseInt(matcher.group(2));
		int patch = Integer.parseInt(matcher.group(3));
		boolean snapshot = matcher.group(4) != null;

		Date buildDate = null;
		if (date != null) {
			try {
				buildDate = DateHelper.parseDateTime(date);
			} catch (ParseException e) {
				// ignore
			}
		}		
		
		return new Version(major, minor, patch, snapshot, buildDate);
	}
	
	public static String getCurrentVersionText() {
		return VERSION;
	}
	
	public static String getCurrentVersionAndBuildDate() {
		return VERSION + " " + DATE;
	}
	
	public static String getCurrentFullBuildMessage() {
		return "Oddjob " + VERSION + " built" + DATE;
	}
	
	public static void main(Object... args) {
		System.out.println(getCurrentFullBuildMessage());
	}
}
