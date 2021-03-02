package org.oddjob.oddballs;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DirectoryOddball2Test {

	
    @Test
	public void testURLs() throws IOException {
		
		DirectoryOddball test = new DirectoryOddball();
		
		File javaHome = new File(System.getProperty("java.home"));
				
		URL[] urls = test.classpathURLs(javaHome);

		assertThat(urls.length > 0, is(true));
	}
}
