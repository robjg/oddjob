package org.oddjob.tools.includes;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.oddjob.util.IO;

public class PlainStreamToText implements StreamToText {

	@Override
	public String load(InputStream input) throws IOException {
		
		BufferedInputStream in = new BufferedInputStream(
				input);
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		IO.copy(in, result);
		
		in.close();
		result.close();
		
		return result.toString();
	}	
}
