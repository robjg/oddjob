package org.oddjob.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

public class CompileJob implements Runnable {

	private File dest;

	private File[] files;
	
	private int result;
	
	@Override
	public void run() {

		if (files == null) {
			throw new IllegalStateException("Nothing to compile.");
		}
		
		List<String> options = new ArrayList<String>();
		
		if (dest != null) {
			options.add("-d");
			options.add(dest.getPath());
		}
		
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        if (compiler == null) {
        	throw new IllegalStateException("No Compile - are you using a JDK not JRE?");
        }
        
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(
        		null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjects(files);
        
        DiagnosticCollector<JavaFileObject> diagnostics = 
        	new DiagnosticCollector<JavaFileObject>();
        
        // reuse the same file manager to allow caching of jar files        
        CompilationTask task = compiler.getTask(null, 
        		fileManager, diagnostics, 
        		options, null, compilationUnits);
        
        if (task.call()) {
        	result = 0;
        }
        else {
        	result = 1;
        }

        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
        	System.out.format("Error on line %d in %d%n",
        			diagnostic.getLineNumber(),
        			diagnostic.getSource());
        }
        
        try {
        	fileManager.close();
        }
        catch (IOException e) {
        	throw new RuntimeException(e);
        }
	}

	public File getDest() {
		return dest;
	}

	public void setDest(File dest) {
		this.dest = dest;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public int getResult() {
		return result;
	}
	
}
