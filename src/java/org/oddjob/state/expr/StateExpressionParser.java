package org.oddjob.state.expr;

import java.util.function.Supplier;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.oddjob.state.antlr.StateConditionBaseListener;
import org.oddjob.state.antlr.StateConditionLexer;
import org.oddjob.state.antlr.StateConditionParser;
import org.oddjob.state.antlr.StateConditionParser.AndContext;
import org.oddjob.state.antlr.StateConditionParser.IsContext;
import org.oddjob.state.antlr.StateConditionParser.NotContext;
import org.oddjob.state.antlr.StateConditionParser.OrContext;

/**
 * Parse a state expression using ANTLR.
 * 
 * @author rob
 *
 * @param <T>
 */
public class StateExpressionParser<T> {

	private final Supplier<StateExpressionCapture<T>> captureSupplier;

	public StateExpressionParser(Supplier<StateExpressionCapture<T>> captureSupplier) {
		this.captureSupplier = captureSupplier;
	}
	
	public T parse(String text) {
				
		StateConditionLexer lexer = new StateConditionLexer(
				CharStreams.fromString(text));
		
		TokenStream tokens = new CommonTokenStream(lexer);
		
		StateConditionParser parser = new StateConditionParser(tokens);
		
		ParseTree tree = parser.stat();
		
		ParseTreeWalker walker = new ParseTreeWalker();

		StateExpressionCapture<T> capture = captureSupplier.get();
		
		ExprListener listener = new ExprListener(capture);
		
		walker.walk(listener, tree);
		
		return capture.getResult();
	}

	static class ExprListener extends StateConditionBaseListener {
		
		private final StateExpressionCapture<?> capture;
		
		public ExprListener(StateExpressionCapture<?> capture) {
			this.capture = capture;
		}
		
		@Override
		public void exitIs(IsContext ctx) {
			capture.is(ctx.job.getText(), ctx.state.getText());
		}
		
		@Override
		public void exitNot(NotContext ctx) {
			capture.not();
		}
		
		@Override
		public void exitAnd(AndContext ctx) {
			capture.and();
		}
		
		@Override
		public void exitOr(OrContext ctx) {
			capture.or();
		}
	}
}
