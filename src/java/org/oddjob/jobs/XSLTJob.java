package org.oddjob.jobs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XSLTJob implements Runnable {

	private InputStream stylesheet;
	
	private InputStream from;
	
	private OutputStream to;
	
	private Map<String, Object> parameters;
	
	public void run() {
		
		if (stylesheet == null) {
			throw new NullPointerException("No Stylesheet.");
		}
		if (from == null) {
			throw new NullPointerException("No From.");
		}
		if (to == null) {
			throw new NullPointerException("No To.");
		}
		
		try {
			Templates templates = TransformerFactory.newInstance(
					).newTemplates(new StreamSource(stylesheet));
			
			Transformer transformer = 
				templates.newTransformer();
		
			if (parameters != null) {
				for (Map.Entry<String, Object> entry: parameters.entrySet()) {
					transformer.setParameter(entry.getKey(), entry.getValue());
				}
			}
			
			transformer.transform(
					new StreamSource(from), 
					new StreamResult(to));
			
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}


	public void setStylesheet(InputStream stylesheet) {
		this.stylesheet = stylesheet;
	}

	public void setFrom(InputStream from) {
		this.from = from;
	}


	public void setTo(OutputStream to) {
		this.to = to;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
}
