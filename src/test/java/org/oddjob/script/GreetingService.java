package org.oddjob.script;

import java.sql.Date;
import java.util.Calendar;

public class GreetingService {

	public String greeting(Date date) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		if (hour < 12) {
			return "Good Morning";
		}
		if (hour < 18) {
			return "Good Afternoon";
		}
		return "Good Evening";
	}
	
	public static String greetPerson(String name) {
		return "Hello " + name;
	}
}
