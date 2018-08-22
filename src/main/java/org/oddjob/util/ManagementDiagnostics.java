package org.oddjob.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

/**
 * Some useful diagnostics. This can be dropped into an
 * Oddjob configuration and run.
 * 
 * @author rob
 *
 */
public class ManagementDiagnostics implements Runnable {

    private String jvmName;
    
    private Date startTime;
    
    private String heapMemory;
    
	private String nonHeapMemory;
    
    @Override
    public void run() {
    	RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean(); 
    	
		jvmName = runtime.getName();
		
		startTime = new Date(runtime.getStartTime());
		
    	MemoryMXBean memory = ManagementFactory.getMemoryMXBean(); 
    	
    	heapMemory = memory.getHeapMemoryUsage().toString();
    	
    	nonHeapMemory = memory.getNonHeapMemoryUsage().toString();
    }
	
	/**
	 * Getter for jvmName.
	 * 
	 * @return the JVM name.
	 */
    public String getJvmName() {
		return jvmName;
	}

    /**
     * Getter for startTime.
     * 
     * @return The JVM start time.
     */
    public Date getStartTime() {
		return startTime;
	}
    
    /**
     * Getter for heapMemory.
     * 
     * @return Description of heap memory usage.
     */
    public String getHeapMemory() {
		return heapMemory;
	}

    /**
     * Getter for nonHeapMemory.
     * 
     * @return Description of non heap memory usage.
     */
	public String getNonHeapMemory() {
		return nonHeapMemory;
	}

}
