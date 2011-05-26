package org.oddjob.oddballs;

import java.io.File;

import org.apache.log4j.Logger;
import org.oddjob.OurDirs;
import org.oddjob.io.CopyJob;
import org.oddjob.io.FilesType;
import org.oddjob.tools.CompileJob;

public class BuildOddballs implements Runnable {
	private static final Logger logger = Logger.getLogger(BuildOddballs.class);

	public void run() {
		build("apple");
		build("orange");
	}
	
	
	public void build(String oddball) {
		
		final OurDirs dirs = new OurDirs();

		File classesDir = new File(dirs.base(), 
				"test/oddballs/" + oddball + "/classes");
		File srcDir = new File(dirs.base(), 
		"test/oddballs/" + oddball + "/src");
		
		if (classesDir.exists()) {
			logger.debug("" + classesDir + 
					" already exists, skipping Oddball build.");
			return;
		}
		else {
			logger.debug("Building Odball classes in: " + classesDir);
			classesDir.mkdir();
		}
		CopyJob copy = new CopyJob();
		copy.setFrom(new File[] { new File(srcDir, "META-INF") });
		copy.setTo(classesDir);
		
		copy.run();
		
		FilesType sources = new FilesType();
		sources.setFiles(dirs.relative(
				"test/oddballs/" + oddball + "/src/fruit").getPath() +
				File.separator + "*.java"); 

		CompileJob compile = new CompileJob();
		
		compile.setDest(classesDir);
		compile.setFiles(sources.toFiles());
		
		compile.run();
		
		if (compile.getResult() != 0) {
			throw new RuntimeException(
					"Compile failed. See standard output for details.");
		}
	}
}
