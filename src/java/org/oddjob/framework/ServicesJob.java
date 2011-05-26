package org.oddjob.framework;

import java.util.LinkedHashMap;
import java.util.Map;

import org.oddjob.arooa.registry.ServiceProvider;
import org.oddjob.arooa.registry.Services;
import org.oddjob.arooa.types.IsType;

/**
 * @oddjob.description Allows objects to be registered that will
 * automatically be injected into subsequent components that
 * are configured for automatic dependency injection.
 * <p>
 * 
 * @oddjob.example
 * 
 * The Development guide has numerous examples using this job.
 * 
 * 
 * @author rob
 *
 */
public class ServicesJob extends SimpleJob 
implements ServiceProvider {

	/**
	 * @oddjob.property registeredServices
	 * @oddjob.description Service definitions. These are simple beans
	 * that define the services being registered. Because of their
	 * simplicity they do not have their own type and can be specified
	 * using the {@link IsType}.
	 * <p>
	 * The properties of the service definition beans are:
	 * <dl>
	 * <dt>service</dt>
	 * <dd>The service object being registered.</dd>
	 * 
	 * <dt>qualifier</dt>
	 * <dd>A qualified that provides extra information for the
	 * type of service.</dd>
	 * 
	 * <dt>intransigent</dt>
	 * <dd>Whether or not to supply a service if the qualifier does
	 * not match that which is required.</dd>
	 * </dl>
	 * 
	 * 
	 * @oddjob.required No, but pointless if missing.
	 */
	private final Map<String, ServiceDefinition> services = 
		new LinkedHashMap<String, ServiceDefinition>();
	
	@Override
	protected int execute() throws Throwable {
		// Configuration starts the service
		
		return 0;
	}
	
	@Override
	protected void onReset() {
		services.clear();
	}
		
	/**
	 * @oddjob.property services
	 * @oddjob.description Provide access to the registered services. 
	 * <p>
	 * Services
	 * are registered by name using object {@code toString} and then if qualified
	 * ';' and the qualifier. If this job has an id {@code my-services} and
	 * the service has a toString of {@code MyCafe} and it is qualified with qualifier
	 * {@code Vegetarian} then it could be referenced as:
	 * <pre>
	 * ${my-services.services.service(MyCafe;Vegetarian)}
	 * </pre>
	 */
	@Override
	public Services getServices() {
		return new Services() {
			
			@Override
			public Object getService(String serviceName)
					throws IllegalArgumentException {

				ServiceDefinition def = services.get(serviceName);
				if (def == null) {
					return null;
				}
				
				return def.getService();
			}
			
			@Override
			public String serviceNameFor(Class<?> theClass, String flavour) {
				
				String best = null;
				
				for (Map.Entry<String, ServiceDefinition> entry : 
					services.entrySet()) {
					
					ServiceDefinition def = entry.getValue();
					if (theClass.isInstance(def.getService())) {
						if (flavour == null ) {
							if (def.getQualifier() == null || 
									!def.isIntransigent())   {
								return entry.getKey();
							}
							else {
								continue;
							}
						}
						
						if (flavour.equals(def.getQualifier())) {
							return entry.getKey();
						}
						
						if (!def.isIntransigent()) {
							best = entry.getKey();
						}
					}
				}
				
				return best;
			}
			
			@Override
			public String toString() {
				return "Registered Services: " + services.size();
			}
		};
	}
	
	public void setRegisteredServices(int index, ServiceDefinition serviceDef) {
		if (serviceDef == null) {
			return;
		}
			
		Object service = serviceDef.getService();
			
		if (service == null) {
			throw new NullPointerException(
					"Service in service definition is null");
		}
		Object qualifier = serviceDef.getQualifier();
		
		String serviceName = service.toString() + 
			(qualifier == null ? "" : ";" + qualifier.toString()); 
		
		logger().info("Registered service " + serviceName + 
				" for types assignable from " + service.getClass().getName());
		
		services.put(serviceName, serviceDef);
	}
	
	/** Definition of a service. */
	public static class ServiceDefinition {
		
		private Object service;
		
		private Object qualifier;

		private boolean  intransigent;
		
		public Object getService() {
			return service;
		}

		public void setService(Object service) {
			this.service = service;
		}

		public Object getQualifier() {
			return qualifier;
		}

		public void setQualifier(Object qualifier) {
			this.qualifier = qualifier;
		}

		public boolean isIntransigent() {
			return intransigent;
		}

		public void setIntransigent(boolean constrained) {
			this.intransigent = constrained;
		}		
	}
}
