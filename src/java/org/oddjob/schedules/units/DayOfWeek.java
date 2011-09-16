package org.oddjob.schedules.units;

import java.util.Arrays;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public interface DayOfWeek {

	enum Days implements DayOfWeek {
		MONDAY {
			@Override
			public int getDayNumber() {
				return 1;
			}
		},
		TUESDAY {
			@Override
			public int getDayNumber() {
				return 2;
			}
		},
		WEDNESDAY {
			@Override
			public int getDayNumber() {
				return 3;
			}
		},
		THURSDAY {
			@Override
			public int getDayNumber() {
				return 4;
			}
		},
		FRIDAY {
			@Override
			public int getDayNumber() {
				return 5;
			}
		},
		SATURDAY{
			@Override
			public int getDayNumber() {
				return 6;
			}
		},
		SUNDAY {
			@Override
			public int getDayNumber() {
				return 7;
			}
		}

	}

	public static class Conversions implements ConversionProvider {
		
		@Override
		public void registerWith(ConversionRegistry registry) {
			
			registry.register(String.class, DayOfWeek.class, 
					new Convertlet<String, DayOfWeek>() {
						 @Override
						public DayOfWeek convert(String from)
								throws ConvertletException {
							try {
								final int day = Integer.parseInt(from);
								return Days.values()[day - 1];
							}
							catch (IndexOutOfBoundsException e) {
								
								throw new ConvertletException("[" + from + 
										"] is not a valid day of week. Valid values are " +
										Arrays.asList(Days.values()) + " or an integer 1 to 7.");
							}
							catch (NumberFormatException e) {
								try {
									return Days.valueOf(from.toUpperCase());
								}
								catch (IllegalArgumentException enumEx) {
									
									throw new ConvertletException("[" + from + 
											"] is not a valid day of week. Valid values are " +
											Arrays.asList(Days.values()) + " or an integer 1 to 7.");
								}
							}
						}
					});
		}
	}
	
	public int getDayNumber();
}
