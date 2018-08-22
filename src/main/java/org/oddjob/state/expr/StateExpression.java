package org.oddjob.state.expr;

import java.util.function.Consumer;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.utils.Try;
import org.oddjob.util.Restore;

/**
 * Something that can evaluate continuously changing state. An example of an expression might be
 * 'job1 is SUCCESS or Job2 is FAILURE'. 
 * 
 * @author rob
 *
 * @see
 */
public interface StateExpression {

	/**
	 * Start evaluating the expression, capturing results in the provided consumer.
	 * 
	 * @param session The session from which the expression can get jobs etc.
	 * @param results The consumer that will continually capture the changing evaluation.
	 * 
	 * @return Something that can be used to stop and cleanup the evaluation.
	 */
	Restore evaluate(ArooaSession session, Consumer<? super Try<Boolean>> results);
	
}
