package org.oddjob.persist;

import org.oddjob.Structural;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * @oddjob.description Browse archives previously create with an {@link ArchiveJob}.
 * 
 * @author rob
 *
 */
public class ArchiveBrowserJob extends SimpleJob implements Structural {

	/** Child helper.
	 */
	protected ChildHelper<Object> childHelper
			= new ChildHelper<Object>(this);
			
	/**
	 * @oddjob.property 
	 * @oddjob.description The name of the archive to browse.
	 * @oddjob.required Yes.
	 */
	private String archiveName;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The persister to use to restore archives.
	 * @oddjob.required Yes, but will fall back on the current Oddjob persister.
	 */
	private OddjobPersister archiver;
	
	@Override
	protected int execute() throws Throwable {
		
		OddjobPersister oddjobPersister = this.archiver;

		if (oddjobPersister == null) {
			ComponentPersister sessionPersister = 
				getArooaSession().getComponentPersister();
			
			if (sessionPersister != null && 
					sessionPersister instanceof OddjobPersister) {
				oddjobPersister = (OddjobPersister) sessionPersister;
			}
			
		}
		
		if (oddjobPersister == null) {
			throw new NullPointerException("No Archiver.");
		}
		
		ComponentPersister persister = oddjobPersister.persisterFor(archiveName);
		
		Object[] archives = persister.list();
		
		int index = 0;
		for (Object archive : archives) {
			childHelper.insertChild(index++, new Restore(
					archive.toString(), persister));
		}
		
		return 0;
	}
	
	@Override
	protected void onReset() {
		childHelper.removeAllChildren();
	}
	
	@Override
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
		
	}
	
	class Restore extends SimpleJob implements Structural {

		private final String archive;
		
		protected ChildHelper<Object> childHelper
			= new ChildHelper<Object>(this);

		private final ComponentPersister archiver;
		
		public Restore(String archive, ComponentPersister archiver) {
			this.archive = archive;
			this.archiver = archiver;
		}
		
		@Override
		protected int execute() throws Throwable {
			
			Object restored = archiver.restore(archive, 
					getClass().getClassLoader(),
					ArchiveBrowserJob.this.getArooaSession());
			
			childHelper.insertChild(0, restored);
			
			return 0;
		}
		
		@Override
		protected void onReset() {
			childHelper.removeAllChildren();
		}
		
		@Override
		public void addStructuralListener(StructuralListener listener) {
			childHelper.addStructuralListener(listener);
		}
		
		@Override
		public void removeStructuralListener(StructuralListener listener) {
			childHelper.removeStructuralListener(listener);
		}
		
		@Override
		public String toString() {
			return archive;
		}
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String path) {
		this.archiveName = path;
	}

	public OddjobPersister getArchiver() {
		return archiver;
	}

	public void setArchiver(OddjobPersister archiver) {
		this.archiver = archiver;
	}
}
