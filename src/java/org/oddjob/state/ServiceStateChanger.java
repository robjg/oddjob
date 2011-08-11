package org.oddjob.state;

import org.oddjob.images.IconHelper;
import org.oddjob.persist.Persistable;

/**
 * A {@link StateChanger} for {@link ServiceState}s.
 * 
 * @author rob
 *
 */public class ServiceStateChanger extends BaseStateChanger<ServiceState> {
		
	public ServiceStateChanger(ServiceStateHandler stateHandler,
			IconHelper iconHelper, Persistable persistable) {
		super(stateHandler, iconHelper, persistable, ServiceState.EXCEPTION);
	}
	
}
