package org.oddjob.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.io.CopyJob;
import org.oddjob.io.FilesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildOddball extends SimpleJob {
	private static final Logger logger = LoggerFactory.getLogger(BuildOddball.class);

	private String oddballDir;
	
	@Override
	protected int execute() throws IOException {

		Path oddballPath = Optional.ofNullable(oddballDir)
				.map(Paths::get)
				.orElse(Paths.get("."));
		
		Path classesDir = oddballPath.resolve("classes");

		Path srcDir = oddballPath.resolve("src");
		
		FilesType sources = new FilesType();
		sources.setFiles(srcDir.toString() + "/**/*.java"); 
		File[] sourceFiles = sources.toFiles();

		if (Files.exists(classesDir)) {			
			if (youngest(sourceFiles) < Files.getLastModifiedTime(classesDir).toMillis()) {
				logger.info("" + classesDir + 
						" up to date, skipping Oddball build.");
				return 0;
			}
			else {
				FileUtils.forceDelete(classesDir.toFile());
			}
		}
	
	
		logger.debug("Building Odball classes in: " + classesDir);

		Files.createDirectory(classesDir);
		
		
		CopyJob copy = new CopyJob();
		copy.setFrom(new File[] { new File(srcDir.toFile(), "META-INF") });
		copy.setTo(classesDir.toFile());
		
		copy.run();
		
		CompileJob compile = new CompileJob();
		
		compile.setDest(classesDir.toFile());
		compile.setFiles(sourceFiles);
		
		compile.run();
		
		if (compile.getResult() != 0) {
			throw new RuntimeException(
					"Compile failed. See standard output for details.");
		}
		
		return 0;		
	}
	
	public String getOddballDir() {
		return oddballDir;
	}

	public void setOddballDir(String oddballDir) {
		this.oddballDir = oddballDir;
	}

	static long youngest(File[] files) {
		
		long youngest = 0;
		
		for (File file : files) {
			youngest = Math.max(youngest, file.lastModified());
		}

		return youngest;
	}
}
