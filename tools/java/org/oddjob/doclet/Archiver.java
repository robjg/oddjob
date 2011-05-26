/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.doclet;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.beandocs.BeanDoc;
import org.oddjob.arooa.beandocs.WriteableBeanDoc;

import com.sun.javadoc.ClassDoc;

/**
 * This class archives away the page data so it may
 * be retrieved later.
 * 
 * @author Rob Gordon.
 */
public class Archiver {

	private final JobsAndTypes jats;
	
    public Archiver(JobsAndTypes jats) {
    	this.jats = jats;
    }
    
    public void archive(ClassDoc classDoc) {
    
    	String fqcn = Processor.fqcnFor(classDoc);

    	WriteableBeanDoc beanDoc = jats.docFor(fqcn);
    	if (beanDoc == null) {
    		return;
    	}
    	
        Processor processor = new Processor(jats, classDoc);
        processor.process();
    }
    
    /**
     * Jobs in index order.
     * 
     * @return An array of PageData objects.
     */
    public IndexLine[] getJobData() {
    	List<IndexLine> lines = new ArrayList<IndexLine>();
    	for (String name : jats.jobs()) {
    		BeanDoc beanDoc = jats.docForJob(name);
    		lines.add(new IndexLine(beanDoc.getClassName(), name,
    				beanDoc.getFirstSentence()));
    	}
    	return lines.toArray(new IndexLine[lines.size()]);
    }
    
    /**
     * Types in index order.
     * 
     * @return An array of PageData objects
     */
    public IndexLine[] getTypeData() {
    	List<IndexLine> lines = new ArrayList<IndexLine>();
    	for (String name : jats.types()) {
    		BeanDoc beanDoc = jats.docForType(name);
    		lines.add(new IndexLine(beanDoc.getClassName(), name,
    				beanDoc.getFirstSentence()));
    	}
    	return lines.toArray(new IndexLine[lines.size()]);
    }
    
    public Iterable<? extends BeanDoc> getAll() {
    	return jats.all();
    }

    
}
