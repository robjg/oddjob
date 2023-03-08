package org.oddjob;

import org.oddjob.arooa.ArooaConstants;
import org.oddjob.arooa.utils.DateTimeHelper;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oddjob's version. Comparison and Equality are only based on the 
 * major minor and patch versions, not on the snapshot or date properties.
 * 
 * @author rob
 *
 */
public class Version implements Comparable<Version>{


	private static final Version current;
	
	static {
		Version version;
		try {
			version = versionFromManifest();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	private final Instant buildDate;
	
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
			boolean snapshot, Instant buildDate) {
		
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.snapshot = snapshot;
		this.buildDate = buildDate;

		this.versionAsText = "" + major + "." + minor + "." + patch +
				(snapshot ? "-SNAPSHOT" : "") +
				(buildDate == null ? "" : " " + buildDate);
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
	public Instant getBuildDate() {
		return buildDate;
	}
	
	@Override
	public int compareTo(Version o) {
		int comparison = Integer.compare(this.major, o.major);
		if (comparison == 0) {
			return 0;
		}
		comparison = Integer.compare(this.minor, o.minor);
		if (comparison == 0) {
			return 0;
		}

		return Integer.compare(this.patch, o.patch);
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

		Instant buildDate = date == null ? null : DateTimeHelper.parseDateTime(date);

		return new Version(major, minor, patch, snapshot, buildDate);
	}

	public static Version versionFromManifest() throws IOException {
		Manifest manifest = manifestFor(Version.class);
		if (manifest == null) {
			return null;
		}
		else {
			Manifest mf = Objects.requireNonNull(
					Version.manifestFor(Version.class), "Failed to Load Manifest for Version");
			Attributes attributes = mf.getMainAttributes();
			String version = attributes.getValue("Implementation-Version");
			if (version == null) {
				return null;
			}
			String buildTime = Objects.requireNonNullElse(
					attributes.getValue("Build-Time"), "Build Time Unknown");

			return Version.versionFor(version, buildTime);
		}
	}

	public static Manifest manifestFor(Class<?> aClass) throws IOException {

		String location = aClass.getProtectionDomain().getCodeSource().getLocation().toString();

		Enumeration<URL> manifests = aClass.getClassLoader().getResources("META-INF/MANIFEST.MF");
		while (manifests.hasMoreElements()) {
			URL url = manifests.nextElement();
			if (url.toString().startsWith(location)) {
				return new Manifest(url.openStream());
			}
		}

		return null;
	}


	public static void main(String... args) {
		System.out.println(current.toString());
	}
}
