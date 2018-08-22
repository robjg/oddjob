package org.oddjob.schedules.units;

import java.util.Arrays;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public interface DayOfMonth {

	enum Shorthands implements DayOfMonth {
		LAST {
			@Override
			public int getDayNumber() {
				return 0;
			}
		},
		PENULTIMATE {
			@Override
			public int getDayNumber() {
				return -1;
			}
		},
	}
	
	public static class Number implements DayOfMonth {
	
		private final int dayNumber;
		
		public Number(int dayNumber) {
			this.dayNumber = dayNumber;
		}
		
		@Override
		public int getDayNumber() {
			return dayNumber;
		}
		
		@Override
		public String toString() {
			return "" + dayNumber;
		}
		
		@Override
		public int hashCode() {
			return dayNumber;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DayOfMonth)) {
				return false;
			}
			return dayNumber == ((DayOfMonth) obj).getDayNumber();
		}
	}
	
	public static class Conversions implements ConversionProvider {
		
		@Override
		public void registerWith(ConversionRegistry registry) {
			
			registry.register(String.class, DayOfMonth.class, 
					new Convertlet<String, DayOfMonth>() {
						 @Override
						public DayOfMonth convert(String from)
								throws ConvertletException {
							try {
								final int day = Integer.parseInt(from);								
								return new Number(day);
							}
							catch (NumberFormatException e) {
								try {
									return Shorthands.valueOf(from.toUpperCase());
								}
								catch (IllegalArgumentException enumEx) {
									
									throw new ConvertletException("[" + from + 
											"] is not a valid day of month. Valid values are " +
											Arrays.asList(Shorthands.values()) + " or an integer.");
								}
							}
						}
					});
		}
	}
	
	public int getDayNumber();
}
