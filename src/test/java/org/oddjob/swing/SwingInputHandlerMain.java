package org.oddjob.swing;

import java.io.File;
import java.util.Properties;

import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.input.InputHandler;
import org.oddjob.input.InputJob;
import org.oddjob.input.requests.InputConfirm;
import org.oddjob.input.requests.InputFile;
import org.oddjob.input.requests.InputMessage;
import org.oddjob.input.requests.InputPassword;
import org.oddjob.input.requests.InputText;

public class SwingInputHandlerMain {

	public static InputJob one(InputHandler handler) {
		
 		InputJob input = new InputJob();
 		input.setArooaSession(new StandardArooaSession());
 		input.setInputHandler(handler);
 		
 		InputConfirm request1 = new InputConfirm();
 		request1.setPrompt("I am over 18\n(Please don't Lie)");
 		request1.setProperty("choice.eighteen");
 		
 		InputText request2 = new InputText();
 		request2.setPrompt("Username\n(For The Database)");
 		request2.setDefault("admin");
 		request2.setProperty("choice.username");
 		
 		InputPassword request3 = new InputPassword();
 		request3.setPrompt("Password\n(For\nthe\ndatabase)");
 		request3.setProperty("choice.password");

 		InputFile request4 = new InputFile();
 		request4.setPrompt("Please Pick a file");
 		request4.setProperty("choice.file");
 		
 		InputMessage request5 = new InputMessage();
 		request5.setMessage("The quick brown fox jumped over the lazy dog.\n" +
 				"It then ate a chicken.");
 		
 		input.setRequests(0, request1);
 		input.setRequests(1, request2);
 		input.setRequests(2, request3);
 		input.setRequests(3, request4);
 		input.setRequests(4, request5);
 		
 		return input;
	}
	
	public static InputJob two(InputHandler handler) {
		
 		InputJob input = new InputJob();
 		input.setName("Simple File Input");
 		input.setArooaSession(new StandardArooaSession());
 		input.setInputHandler(handler);
 		
 		InputFile request1 = new InputFile();
 		request1.setPrompt("Please Pick a file");
 		request1.setCurrentDirectory(new File("/users/rob"));
 		request1.setDefault("apples.txt");
 		request1.setFileFilterExtensions(new String[] { "txt", "doc" });
 		request1.setProperty("choose.file");
 		
 		input.setRequests(0, request1);
 		
 		return input;
	}
	
	public static void main(String... args) {

//		StdInInputHandler handler = new StdInInputHandler();
		SwingInputHandler handler = new SwingInputHandler(null);

		InputJob[] inputs = new InputJob[] {
//			one(handler),
			two(handler),
		};		
		
		for (InputJob input : inputs) {
			
			System.out.println("------- " + input.getName() + "---------");
			
			input.run();
						
			Properties props = input.getProperties();
			if (props != null) {
				for (Object key : props.keySet()) {
					System.out.println(key + "=" + props.get(key));
				}
			}			
		}				
	}
}
