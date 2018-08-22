package org.oddjob.state.expr;

/**
 * Something that can be used to capture the results of traversing the parser syntax tree for a
 * state expression.
 * 
 * @author rob
 *
 * @param <T> The type of the result.
 * 
 * @see StateExpressionParser
 */
public interface StateExpressionCapture<T> {
	
	void is(String job, String state);
	
	void not();
	
	void and();
	
	void or();
	
	T getResult();
}
