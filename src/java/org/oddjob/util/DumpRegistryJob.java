package org.oddjob.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.reflect.BeanViewBean;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.io.StdoutType;
import org.oddjob.jobs.BeanReportJob;

/**
 * Dumps Oddjob Bean Registry and Component Pools for debugging purposes.
 * <p>
 * Example:
 * 
 * {@oddjob.xml.resource org/oddjob/util/DumpRegistryJobExample.xml}
 * 
 * The output is:
 * 
 * {@oddjob.text.resource org/oddjob/util/DumpResistryJobExample.txt}
 * 
 * 
 * @author rob
 *
 */
public class DumpRegistryJob implements Runnable, ArooaSessionAware {

	private volatile ArooaSession session;
	
	private volatile OutputStream output;
	
	
	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}

	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	public void run() {
		
		if (output == null) {
			try {
				output = new StdoutType().toValue();
			} catch (ArooaConversionException e) {
				throw new RuntimeException(e);
			}
		}						
		
		PrintStream out = new PrintStream(output);
		
		try {

			out.println();
			out.println("Bean Directory:");
			out.println("===============");
			
			BeanDirectory reg = session.getBeanRegistry();
			
			dumpBeanDirectory(reg, out);
			
			out.println();
			out.println("Component Pool:");
			out.println("===============");
			
			ComponentPool pool = session.getComponentPool();
			
			dumpComponentPool(pool, out);
			
		}
		finally {
			out.close();
		}
	}
	
	public static class BeanDirectoryInfo {
		
		private final String id;
		private final String toString;
		private final Class<?> type;
		private final long identityHash;
		
		public BeanDirectoryInfo(String id, String toString, 
				Class<?> type, long idendityHash) {
			this.id = id;
			this.toString = toString;
			this.type = type;
			this.identityHash = idendityHash;
		}
		
		public String getId() {
			return id;
		}
		
		public String getToString() {
			return toString;
		}
		
		public long getIdentityHash() {
			return identityHash;
		}
		
		public String getTypeName() {
			return type.getName();
		}
	}
	
	
	void dumpBeanDirectory(BeanDirectory reg, PrintStream out) {
		
		List<BeanDirectoryInfo> infoList = 
				new ArrayList<BeanDirectoryInfo>();
		
		for (Object component : reg.getAllByType(Object.class)) {
			
			BeanDirectoryInfo info = new BeanDirectoryInfo(
					reg.getIdFor(component), component.toString() , 
					component.getClass(), 
					System.identityHashCode(component));
			
			infoList.add(info);
		}
		
		report(infoList, out);
		
	}
	
	void dumpComponentPool(ComponentPool pool, PrintStream out) {
		
		List<BeanDirectoryInfo> infoList = 
				new ArrayList<BeanDirectoryInfo>();
		
		for (ComponentTrinity trinity : pool.allTrinities()) {
			
			Object component = trinity.getTheComponent();
					
			BeanDirectoryInfo info = new BeanDirectoryInfo(
					pool.getIdFor(component), component.toString() , 
					component.getClass(), 
					System.identityHashCode(component));

			infoList.add(info);
		}

		report(infoList, out);
	}
	
	public void report(List<? extends Object> beans, OutputStream out) {
		
		BeanViewBean beanView = new BeanViewBean();
		beanView.setProperties("id, toString, typeName, identityHash");
		beanView.setTitles("Id, Name, Class, Identity");
		
		BeanReportJob report = new BeanReportJob();
		report.setArooaSession(session);
		report.setBeanView(beanView.toValue());
		
		report.setOutput(new FilterOutputStream(out) {
			@Override
			public void close() throws IOException {
				super.flush();
			}
		});
		report.setBeans(beans);
		report.run();
		
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
