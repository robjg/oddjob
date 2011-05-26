/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.images;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Locale;

/**
 *
 * @author Rob Gordon.
 */
public class IconTip implements Serializable {
	private static final long serialVersionUID = 20051114;
	
	private final byte[] imageData;
	private final String toolTip;
	
	public IconTip(byte[] imageData, String toolTip) {
		this.imageData = imageData;
		this.toolTip = toolTip;
	}
	
	public IconTip(URL url, String toolTip) {
		byte[] imageData = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = url.openStream();
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			imageData = out.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.imageData = imageData;
		this.toolTip = toolTip;
	}
	
	public byte[] getImageData() {
		return this.imageData;
	}
	
	public String getToolTip() {
		return this.toolTip;
	}
	
	public String getToolTip(Locale local) {
		return getToolTip();
	}
}
