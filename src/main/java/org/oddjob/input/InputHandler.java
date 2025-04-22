package org.oddjob.input;

import java.util.Properties;

/**
 * Something capable of providing input to Oddjob.
 * 
 * @author rob
 *
 */
public interface InputHandler {


	Session start();

	interface Session extends AutoCloseable {


		/**
		 * Handle a series of input requests. The series is intended to be
		 * short and simple such as username and password.
		 *
		 * @param requests The requests.
		 *
		 * @return The results. These are serializable so they can be persisted
		 * by Oddjob.
		 */
		Properties handleInput(InputRequest[] requests);

		@Override
		default void close() {}

	}

}
