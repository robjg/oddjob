package org.oddjob.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.oddjob.FailedToStopException;

/**
 * A collection of different strategies that are applied to a component
 * to see if it can be adapted to a {@link Service}. 
 * 
 * @author rob
 *
 */
public class ServiceStrategies implements ServiceStrategy {

	@Override
	public ServiceAdaptor serviceFor(Object component) {
		
		ServiceAdaptor adaptor = 
				isServiceAlreadyStrategy().serviceFor(component);
		if (adaptor == null) {
			adaptor = hasServiceAnnotationsStrategy().serviceFor(component);
		}
		if (adaptor == null) {
			adaptor = hasServiceMethodsStrategy().serviceFor(component);
		}
		return adaptor;
	}
	
	/**
	 * Provides a strategy that checks to see if the component is a
	 * {@link Service} already.
	 *  
	 * @return
	 */
	public ServiceStrategy isServiceAlreadyStrategy() {
		return new ServiceStrategy() {
			
			@Override
			public ServiceAdaptor serviceFor(Object component) {
				if (component instanceof Service) {
					final Service service = (Service) component;
					return new ServiceAdaptor() {
						
						@Override
						public void start() throws Exception {
							service.start();
						}
						
						@Override
						public void stop() throws FailedToStopException {
							service.stop();
						}
						
						@Override
						public Object getComponent() {
							return service;
						}
					};
				}
				else {
					return null;
				}
			}
		};
	}
	
	public ServiceStrategy hasServiceMethodsStrategy() {
		return new ServiceStrategy() {
			
			public ServiceMethodAdaptor serviceFor(Object component) {
				Class<?> cl = component.getClass();
				try {
					Method startMethod = cl.getDeclaredMethod(
							"start", new Class[0]);
					if (startMethod.getReturnType() != Void.TYPE) {
						return null;
					}
					Method stopMethod = cl.getDeclaredMethod(
							"stop", new Class[0]);
					if (startMethod.getReturnType() != Void.TYPE) {
						return null;
					}
					return new ServiceMethodAdaptor(
							component, startMethod, stopMethod);
				} catch (Exception e) {
					return null;			
				}
			}
		};
	}
	
	public ServiceStrategy hasServiceAnnotationsStrategy() {
		return new ServiceStrategy() {
			
			@Override
			public ServiceAdaptor serviceFor(Object component) {
				
				Method startMethod = null;
				Method stopMethod = null;
				
				Class<?> cl = component.getClass();
				for (Method method : cl.getMethods()) {

					Annotation[] annotations = method.getAnnotations();
					
					for (Annotation arooaAnnotation: annotations) {
						
						if (arooaAnnotation instanceof Start) {
							if (startMethod != null) {
								throw new IllegalStateException(
									"Class " + cl.getName() + 
									" has more than on @Start annotation.");
							}
							startMethod = method;
						}
						if (arooaAnnotation instanceof Stop) {
							if (stopMethod != null) {
								throw new IllegalStateException(
									"Class " + cl.getName() + 
									" has more than on @Stop annotation.");
							}
							stopMethod = method;
						}
					}
				}
				if (startMethod == null && stopMethod == null) {
					return null;
				}
				if (startMethod != null && stopMethod != null) {
					return new ServiceMethodAdaptor(
							component, startMethod, stopMethod);
				}
				throw new IllegalStateException(
					"Class " + cl.getName() + 
					" must have both a @Start and a @Stop method annoted.");
			}
		};
	}
}
