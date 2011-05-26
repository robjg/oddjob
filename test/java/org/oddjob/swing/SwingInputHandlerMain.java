package org.oddjob.swing;

import java.util.Properties;

import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.input.InputJob;
import org.oddjob.input.requests.InputConfirm;
import org.oddjob.input.requests.InputMessage;
import org.oddjob.input.requests.InputPassword;
import org.oddjob.input.requests.InputText;

public class SwingInputHandlerMain {

	public static void main(String... args) {
		
		SwingInputHandler handler = new SwingInputHandler(null);
		
 		InputJob input = new InputJob();
 		input.setArooaSession(new StandardArooaSession());
 		input.setInputHandler(handler);
 		
 		InputConfirm request1 = new InputConfirm();
 		request1.setPrompt("I am over 18");
 		request1.setProperty("choice.eighteen");
 		
 		InputText request2 = new InputText();
 		request2.setPrompt("Username");
 		request2.setDefault("admin");
 		request2.setProperty("choice.username");
 		
 		InputPassword request3 = new InputPassword();
 		request3.setPrompt("Password");
 		request3.setProperty("choice.password");

 		InputMessage request4 = new InputMessage();
 		request4.setMessage("The quick brown fox jumped over the lazy dog.");
 		
 		input.setRequests(0, request1);
 		input.setRequests(1, request2);
 		input.setRequests(2, request3);
 		input.setRequests(3, request4);
 		
		input.run();
		
		Properties props = input.getProperties();
		if (props != null) {
			for (Object key : props.keySet()) {
				System.out.println(key + "=" + props.get(key));
			}
		}
	}
}
