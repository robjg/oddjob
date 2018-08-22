/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * IO Utilities.
 */
public class IO {

	
	/**
	 * Copy from one stream to another.
	 * 
	 * @param in InputStream.
	 * @param out OutputStream.
	 * 
	 * @throws IOException If copy fails.
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException{
		byte b[] = new byte[8192];
		int i;
		while ((i = in.read(b)) != -1) {
			out.write(b, 0, i);
		}		
	}

	/**
	 * Can the object be serialized.
	 * 
	 * @param o The object.
	 * @return true if it can, false if it can't.
	 */
	public static boolean canSerialize(Object o) {
		if (o == null) {
			return true;
		}
		if (!(o instanceof Serializable)) {
			return false;
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutputStream s = new ObjectOutputStream(os);
			s.writeObject(o);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}
