/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.beandocs.BeanDoc;
import org.oddjob.arooa.beandocs.ExampleDoc;
import org.oddjob.arooa.beandocs.PropertyDoc;

/**
 * Creates the reference files.
 * 
 * @author Rob Gordon.
 */
public class ManualWriter {
	
	public static final String COPYWRITE = "(c) Rob Gordon 2005 - 2017";
	
    private final File directory;
   
    private final String title;
    
	public ManualWriter(String directory, String title) {
		this.directory = new File(directory);
		this.title = title == null ? "Oddjob Reference" : title;
	}
	
	
    /**
     * Write a single reference page.
     * 
     * @param beanDoc
     */
    public void writePage(BeanDoc beanDoc) {
    	
    	PrintWriter out = null;

    	try {
        	File file = new File(directory, getFileName(beanDoc.getClassName()));
        	file.getParentFile().mkdirs();
            out = new PrintWriter(
                new FileOutputStream(file));
    	}
        catch (IOException e) {
            throw new RuntimeException(e);

        }

        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>" + title + " - " + 
        		beanDoc.getName() + "</title>");
        out.println("  </head>");
        out.println("  <body>");
        out.println("  [<a href=\"" + getIndexFile(beanDoc.getClassName()) +
        		"\">Index</a>]");
        out.println("    <h1>" + beanDoc.getName() + "</h1>");
        if (beanDoc.getAllText() != null) {
        	out.println("    <hr/>");
        	out.println(beanDoc.getAllText());
        }

        PropertyDoc[] propertyDocs = beanDoc.getPropertyDocs();

        if (propertyDocs.length > 0) {
        	out.println("    <hr/>");
        	out.println("    <h3>Property Summary</h3>");
        	out.println("    <table width='100%' border='1'" +
        	" cellpadding='3' cellspacing='0'>");
        	int i = 0;
        	for (PropertyDoc elem : propertyDocs ) {
        		if (ConfiguredHow.HIDDEN == elem.getConfiguredHow()) {
        			continue;
        		}
        		out.println("    <tr>");
        		out.println("      <td><a href='#property" + ++i + "'>" 
        				+ elem.getPropertyName() + "</a></td>");
        		out.println("      <td>" + (elem.getFirstSentence() == null
        				? "&nbsp;" : elem.getFirstSentence()) + 
        		"</td>");
        		out.println("    </tr>");
        	}
        	out.println("    </table>");
        }

        ExampleDoc[] exampleDocs = beanDoc.getExampleDocs();

        if (exampleDocs.length > 0) {
        	out.println("    <hr/>");
        	out.println("    <h3>Example Summary</h3>");
        	out.println("    <table width='100%' border='1'" +
        	" cellpadding='3' cellspacing='0'>");
        	int i = 0;
        	for (ExampleDoc elem : exampleDocs ) {
        		out.println("    <tr>");
        		out.println("      <td><a href='#example" + ++i + 
        				"'>Example " + i +  "</a></td>");
        		out.println("      <td>" + (elem.getFirstSentence() == null
        				? "&nbsp;" : elem.getFirstSentence()) + 
        		"</td>");
        		out.println("    </tr>");
        	}
        	out.println("    </table>");
        }

        if (propertyDocs.length > 0) {
        	out.println("    <hr/>");
        	out.println("    <h3>Property Detail</h3>");
        	int i = 0;
        	for (PropertyDoc elem : propertyDocs ) {
        		if (ConfiguredHow.HIDDEN == elem.getConfiguredHow()) {
        			continue;
        		}
        		out.println("    <a name='property" + ++i + "'><h4>" + 
        				elem.getPropertyName() + "</h4></a>");
        		out.println("      <table style='font-size:smaller'>");
        		if (elem.getAccess() != PropertyDoc.Access.READ_ONLY) {
        			out.println("      <tr><td><i>Configured By</i></td><td>" + 
        					elem.getConfiguredHow() + "</td></tr>");
        		}
        		out.println("      <tr><td><i>Access</i></td><td>" + 
        				elem.getAccess() + "</td></tr>");
        		String required = elem.getRequired();
        		if (required != null) {
        			out.println("      <tr><td><i>Required</i></td><td>" + 
        					required + "</td></tr>");
        		}
        		out.println("      </table>");
        		out.println("      <p>");
        		out.println(elem.getAllText() == null ? 
        				"" : elem.getAllText());
        		out.println("      </p>");
        	}
        }

        if (exampleDocs.length > 0) {
        	out.println("    <hr/>");
        	out.println("    <h3>Examples</h3>");
        	int i = 0;
        	for (ExampleDoc example : exampleDocs) {
        		out.println("    <a name='example" + ++i + 
        				"'><h4>Example " + i + "</h4></a>");
        		out.println("    <p>");
        		out.println(example.getAllText());
        		out.println("    </p>");
        	}
        }
        out.println("    <hr/>");
        out.println("    <font size='-1' align='center'>" + COPYWRITE + "</font>");
        out.println("	 </body>");
        out.println("  </html>");

        out.close();
    }

    /**
     * Create the index.
     * 
     * @param jobs
     * @param types
     */
    public void writeIndex(IndexLine[] jobs, IndexLine[] types) {
    	PrintWriter out = null;
    	
    	try {
            out = new PrintWriter(
                new FileOutputStream(
                        new File(directory, "index.html")));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    	
        out.println("<html>");
        out.println("  <head>");
        out.println("  [<a href=\"../index.html\">Home</a>]");
        out.println("    <title>" + title + " - Contents</title>");
        out.println("  </head>");
        out.println("  <body>");
        out.println("    <h2>" + title + "</h2>");
        out.println("    <ul>");
        out.println("    <li>Jobs");
        out.println("      <ul>");
        for (int i = 0; i < jobs.length; ++i) {
        	IndexLine beanDoc = jobs[i];
        	out.println("        <li>");
        	out.println("          <a href='" + getFileName(
        			beanDoc.getClassName())
        			+ "'>" + beanDoc.getName() + 
        			"</a> - " + beanDoc.getFirstSentence());
        	out.println("        </li>");
        }
        out.println("      </ul></li>");
        out.println("    <li>Types");
        out.println("      <ul>");
        for (int i = 0; i < types.length; ++i) {
        	IndexLine beanDoc = types[i];
        	out.println("        <li>");
        	out.println("          <a href='" + getFileName(
        			beanDoc.getClassName()) 
        			+ "'>" + beanDoc.getName() + 
        			"</a> - " + beanDoc.getFirstSentence());
        	out.println("        </li>");
        }
        out.println("      </ul></li>");
        out.println("    </ul>");

        out.println("    <hr/>");
        out.println("    <font size='-1' align='center'>" + COPYWRITE + "</font>");
        out.println("	 </body>");
        out.println("  </html>");
        
        out.close();        
    }
    
    public void writeAll(Iterable<? extends BeanDoc> all) {
        for (BeanDoc beanDoc : all ) {
            writePage(beanDoc);
        }
    }
        
    public void createManual(Archiver archiver) {
        writeIndex(archiver.getJobData(), archiver.getTypeData());
        writeAll(archiver.getAll());
   	
    }
    
    /**
     * Get the file name the page data should be created in.
     * 
     * @return The file name.
     */
    public String getFileName(String className) {
        return className.replace('.', '/') + ".html";
    }
    
    /**
     * Get the index file. This is a reference back and will depend
     * on the package depth.
     * 
     * @return The file name.
     */
    public static String getIndexFile(String className) {
    	
    	String path = "";
    	int start = 0;
    	while ((start = className.indexOf('.', start) + 1) > 0) {
    		path = path + "../";
    	}
    	return path + "index.html";
    }
}
