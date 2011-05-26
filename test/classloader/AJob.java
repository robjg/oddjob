
import org.oddjob.framework.SimpleJob;

public class AJob extends SimpleJob {
	
	ClassLoader classLoader;
	
	@Override
	protected int execute() throws Throwable {
		this.classLoader = Thread.currentThread().getContextClassLoader();
		System.out.println("" + getClass().getClassLoader());
		System.out.println("Worked.");
		return 0;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}