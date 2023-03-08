package org.oddjob;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.arooa.utils.DateTimeHelper;

import java.io.IOException;
import java.time.Instant;

public class VersionTest extends OjTestCase {

    @Test
    public void testToString() {

        Version test = new Version(1, 2, 3);

        assertEquals("1.2.3", test.toString());

        test = new Version(1, 2, 3, true, null);

        assertEquals("1.2.3-SNAPSHOT", test.toString());

        test = new Version(1, 2, 3, true,
                DateTimeHelper.parseDateTime("2013-09-08 09:45"));

        assertEquals("1.2.3-SNAPSHOT 2013-09-08T08:45:00Z", test.toString());
    }

    @Test
    public void testCreating() {

        Version test = Version.versionFor("1.2.3", null);

        MatcherAssert.assertThat(test, Matchers.notNullValue());

        assertEquals("1.2.3", test.toString());

        test = Version.versionFor("1.2.3-SNAPSHOT", null);

        MatcherAssert.assertThat(test, Matchers.notNullValue());

        assertEquals("1.2.3-SNAPSHOT", test.toString());

        test = Version.versionFor("1.2.3", "2013-09-08 09:45");

        MatcherAssert.assertThat(test, Matchers.notNullValue());

        assertEquals("1.2.3 2013-09-08T08:45:00Z", test.toString());

        MatcherAssert.assertThat(test, Matchers.notNullValue());

        test = Version.versionFor("1.2.3", "2013-09-08 09:45");

        MatcherAssert.assertThat(test, Matchers.notNullValue());

        assertEquals("1.2.3 2013-09-08T08:45:00Z", test.toString());

        test = Version.versionFor("one.two.three", "2013-09-08 09:45");

        assertEquals(null, test);
    }

    public void tesComparison() {

        Version test1 = new Version(1, 2, 3);

        Version test2 = new Version(2, 2, 3);

        assertEquals(true, test1.compareTo(test2) < 0);
        assertEquals(true, test2.compareTo(test1) > 0);

        test1 = new Version(1, 2, 3);

        test2 = new Version(1, 3, 3);

        assertEquals(true, test1.compareTo(test2) < 0);
        assertEquals(true, test2.compareTo(test1) > 0);

        test1 = new Version(1, 2, 3);

        test2 = new Version(1, 2, 4);

        assertEquals(true, test1.compareTo(test2) < 0);
        assertEquals(true, test2.compareTo(test1) > 0);

        test1 = new Version(1, 2, 3);

        test2 = new Version(1, 2, 3);

        assertEquals(0, test1.compareTo(test2));
        assertEquals(0, test2.compareTo(test1));
    }

    @Test
    public void testEquals() {

        Version test1 = new Version(1, 2, 3);

        Version test2 = new Version(1, 2, 3);

        assertEquals(true, test1.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
    }

    @Test
    public void versionFromMainifest() throws IOException {

        Version version = Version.versionFromManifest();

        MatcherAssert.assertThat(version, Matchers.notNullValue());

        MatcherAssert.assertThat(version.getMajor(), Matchers.is(1));
        MatcherAssert.assertThat(version.getMinor(), Matchers.is(7));
        MatcherAssert.assertThat(version.getPatch(), Matchers.is(0));
        MatcherAssert.assertThat(version.getBuildDate(), Matchers.is(Instant.parse("2023-03-01T00:00:00Z")));

        MatcherAssert.assertThat(version.toString(), Matchers.is("1.7.0-SNAPSHOT 2023-03-01T00:00:00Z"));
    }
}
