package org.oddjob.images;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.oddjob.OjTestCase;

public class ImageIconStableTest extends OjTestCase {

	ImageIcon copy;
	
   @Test
	public void testSerialize() throws IOException, ClassNotFoundException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);
		
		URL url = IconHelper.class.getResource("cross.gif");
		
		oos.writeObject(new ImageIconStable(url, "A Cross"));
		oos.close();
		
		ByteArrayInputStream in = new ByteArrayInputStream(
				out.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(in);
		
		copy = (ImageIcon) ois.readObject();
		
		assertNotNull(copy);
	}
	
	public static void main(String... args) throws IOException, ClassNotFoundException {
		
		ImageIconStableTest test = new ImageIconStableTest();
		test.testSerialize();
		
		JOptionPane.showConfirmDialog(null, "Hi", "Message", 0, 0, 
				test.copy);
	}
}
