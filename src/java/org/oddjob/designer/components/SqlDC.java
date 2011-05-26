/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TabGroup;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.sql.SQLJob;

/**
 * {@link DesignFactory} for {@link SQLJob}
 */
public class SqlDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new SqlDesign(element, parentContext);
	}
}

class SqlDesign extends BaseDC {

	private final SimpleDesignProperty connection;
	
	private final SimpleDesignProperty input;
	
	private final IndexedDesignProperty parameters;
	
	private final SimpleTextAttribute callable;
	
	private final SimpleTextAttribute escapeProcessing;
	
	private final SimpleTextAttribute onError;
	
	private final SimpleDesignProperty results;
	
	private final SimpleTextAttribute expandProperties;
	
	private final SimpleTextAttribute encoding;
	
	private final SimpleTextAttribute delimiter;
	
	private final SimpleTextAttribute delimiterType;
	
	private final SimpleTextAttribute keepFormat;

	public SqlDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		connection = new SimpleDesignProperty("connection", this);
		
		input = new SimpleDesignProperty("input", this);
		
		parameters = new IndexedDesignProperty("parameters", this);
		
		callable = new SimpleTextAttribute("callable", this);
		
		escapeProcessing = new SimpleTextAttribute("escapeProcessing", this);
		
		onError = new SimpleTextAttribute("onError", this);
		
		results = new SimpleDesignProperty("results", this);
		
		expandProperties = new SimpleTextAttribute("expandProperties", this);
		
		encoding = new SimpleTextAttribute("encoding", this);
		
		delimiter = new SimpleTextAttribute("delimiter", this);
		
		delimiterType = new SimpleTextAttribute("delimiterType", this);
		
		keepFormat = new SimpleTextAttribute("keepFormat", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(new BorderedGroup("Essentials")
				.add(connection.view().setTitle("Connection"))
				.add(input.view().setTitle("Input for SQL"))
				)
			.addFormItem(
				new TabGroup()
					.add(new FieldGroup("Execution Options")
						.add(parameters.view().setTitle("Parameters"))
						.add(callable.view().setTitle("Callable"))
						.add(escapeProcessing.view().setTitle("Escape Processing"))
						.add(onError.view().setTitle("On Error"))
						.add(results.view().setTitle("Result Processor"))
					)
					.add(new FieldGroup("Parse Options")
						.add(expandProperties.view().setTitle("Expand Properties"))
						.add(delimiter.view().setTitle("Delimiter"))
						.add(delimiterType.view().setTitle("Delimiter Type"))
						.add(keepFormat.view().setTitle("Keep Format"))
						.add(encoding.view().setTitle("Encoding"))
					)
				);					
	}
			
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, connection, input, 
				parameters, callable, escapeProcessing, onError, results,
				expandProperties, delimiter, delimiterType, keepFormat, encoding};
	}
}
