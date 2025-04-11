package org.oddjob.input;

import org.oddjob.FailedToStopException;
import org.oddjob.Stoppable;
import org.oddjob.arooa.utils.ListSetterHelper;
import org.oddjob.values.properties.PropertiesJobBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @oddjob.description Ask for input from the user. 
 * <p>
 * The medium with 
 * which Oddjob asks for input will depend on how it's running. When
 * running in Oddjob Explorer a GUI dialogue will be used. When running
 * from the console, input from the console will be requested.
 * 
 * @oddjob.example
 * 
 * Request lots of input.
 * <p>
 * {@oddjob.xml.resource org/oddjob/input/InputHandlerExample.xml}
 * 
 * @author rob
 *
 */
public class InputJob extends PropertiesJobBase implements Stoppable {
	private static final long serialVersionUID = 2011011700L;

	private static final Logger logger = LoggerFactory.getLogger(InputJob.class);
	/**
	 * @oddjob.property 
	 * @oddjob.description The input handler to use.
	 * @oddjob.required No. This will be set automatically by Oddjob.
	 */
	private transient InputHandler inputHandler;
	
	/**
	 * @oddjob.property
	 * @oddjob.description A list of requests for input.
	 * @oddjob.required No, but there will be no values.
	 */
	private transient List<InputRequest> requests;
		
	public InputJob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		requests = new ArrayList<>();
	}

	private AutoCloseable close;

	@Override
	protected int execute() {
		
		if (inputHandler == null) {
			throw new NullPointerException("No InputHandler");
		}
		Properties props;
		
		InputRequest[] requestsArray = requests.toArray(new InputRequest[0]);

		InputHandler.Session session = inputHandler.start();
		this.close = session;

		props = session.handleInput(requestsArray);

		close = null;

		if (props == null) {
			logger.info("No Input.");
			return 1;
		}
		else {
			logger.info("Input complete. [" + props.size()+ "] set.");
			setProperties(props);
			addPropertyLookup();
			return 0;
		}
	}

	@Override
	protected void onStop() throws FailedToStopException {
		AutoCloseable closeable = this.close;
		if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new FailedToStopException(this, e);
            }
        }
	}

	public InputHandler getInputHandler() {
		return inputHandler;
	}

	@Inject
	public void setInputHandler(InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}

	public InputRequest getRequests(int index) {
		return requests.get(index);
	}

	public void setRequests(int index, InputRequest request) {
		new ListSetterHelper<InputRequest>(requests).set(index, request);
	}

	@Override
	protected boolean isOverride() {
		return true;
	}
	
	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		completeConstruction();
	}
}
