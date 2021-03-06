package org.oddjob.oddballs;

import org.oddjob.OurDirs;
import org.oddjob.io.CopyJob;
import org.oddjob.io.FilesType;
import org.oddjob.tools.CompileJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class BuildOddballs implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(BuildOddballs.class);

	private final String oddballsDir;
	
	public BuildOddballs() {
		this("test/oddballs/");
	}
	
	public BuildOddballs(String oddballsDir) {
		this.oddballsDir = oddballsDir;
	}
	
	
	public void run() {
		try {
			build("apple");
			build("orange");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void build(String oddball) throws IOException {
		
		final OurDirs dirs = new OurDirs();

		File classesDir = new File(dirs.base(), 
				oddballsDir + oddball + "/classes");
		File srcDir = new File(dirs.base(), 
		oddballsDir + oddball + "/src");
		
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
				oddballsDir + oddball + "/src/fruit").getPath() +
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
