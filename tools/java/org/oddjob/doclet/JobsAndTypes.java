/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.doclet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oddjob.arooa.beandocs.BeanDoc;
import org.oddjob.arooa.beandocs.WriteableArooaDoc;
import org.oddjob.arooa.beandocs.WriteableBeanDoc;

/**
 * Collects together the jobs and types the reference will reference.
 * 
 * @author Rob Gordon.
 */
public class JobsAndTypes {

	private final Map<String, BeanDoc> jobDocs = 
		new LinkedHashMap<String, BeanDoc>();
	
	private final Map<String, BeanDoc> typeDocs = 
		new LinkedHashMap<String, BeanDoc>();
	
	
	/** Map of fqcn to tag for jobs. */
    private final Map<String, WriteableBeanDoc> docsByName = 
    	new HashMap<String, WriteableBeanDoc>();
    
    public JobsAndTypes(WriteableArooaDoc jobDocs, WriteableArooaDoc typeDocs) {   
    	
    	loadProps(jobDocs, this.jobDocs);
    	
		loadProps(typeDocs, this.typeDocs);
    }

    void loadProps(WriteableArooaDoc doc, 
    		Map<String, BeanDoc> into) {
    	    	
    	WriteableBeanDoc[] beanDocs = doc.getBeanDocs();
    	
		for (WriteableBeanDoc beanDoc: beanDocs) {
			
			String className = beanDoc.getClassName();

			// is, bean are jobs and types - they need to share the doc.
			WriteableBeanDoc list = docsByName.get(className);
			if (list == null) {
				list = beanDoc;
				docsByName.put(className, list);
			}
			into.put(beanDoc.getName(), list);
		}		
    }
    
    public WriteableBeanDoc docFor(String fqcn) {
    	return docsByName.get(fqcn);
    }
        
    /**
     */
    public Iterable<String> types(){
    	return typeDocs.keySet(); 
    }
    
    public BeanDoc docForType(String name) {
    	return typeDocs.get(name);
    }
    
    /**
     */
    public Iterable<String> jobs(){
    	return jobDocs.keySet(); 
    }
    
    public BeanDoc docForJob(String name) {
    	return jobDocs.get(name);
    }
    
    public Iterable<? extends BeanDoc> all() {
    	return docsByName.values();
    }
}
