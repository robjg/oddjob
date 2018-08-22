package org.oddjob.schedules.units;

import java.util.Arrays;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public interface Month {

	public enum Months implements Month {
		JANUARY {
			@Override
			public int getMonthNumber() {
				return 1;
			}
		},
		FEBRUARY {
			@Override
			public int getMonthNumber() {
				return 2;
			}
		},
		MARCH {
			@Override
			public int getMonthNumber() {
				return 3;
			}
		},
		APRIL {
			@Override
			public int getMonthNumber() {
				return 4;
			}
		},
		MAY {
			@Override
			public int getMonthNumber() {
				return 5;
			}
		},
		JUNE {
			@Override
			public int getMonthNumber() {
				return 6;
			}
		},
		JULY {
			@Override
			public int getMonthNumber() {
				return 7;
			}
		},
		AUGUST {
			@Override
			public int getMonthNumber() {
				return 8;
			}
		},
		SEPTEMBER {
			@Override
			public int getMonthNumber() {
				return 9;
			}
		},
		OCTOBER {
			@Override
			public int getMonthNumber() {
				return 10;
			}
		},
		NOVEMBER {
			@Override
			public int getMonthNumber() {
				return 11;
			}
		},
		DECEMBER {
			@Override
			public int getMonthNumber() {
				return 12;
			}
		},
	}
	
	public static class Conversions implements ConversionProvider {
		
		@Override
		public void registerWith(ConversionRegistry registry) {
			
			registry.register(String.class, Month.class, 
					new Convertlet<String, Month>() {
						 @Override
						public Month convert(String from)
								throws ConvertletException {
							try {
								int month = Integer.parseInt(from);
								return Months.values()[month - 1];
							}
							catch (IndexOutOfBoundsException e) {
								
								throw new ConvertletException("[" + from + 
										"] is not a valid month. Valid values are " +
										Arrays.asList(Months.values()) + " or an integer 1 to 12.");
							}
							catch (NumberFormatException e) {
								try {
									return Months.valueOf(from.toUpperCase());
								}
								catch (IllegalArgumentException enumEx) {
									
									throw new ConvertletException("[" + from + 
											"] is not a valid month. Valid values are " +
											Arrays.asList(Months.values()) + " or an integer 1 to 12.");
								}
							}
						}
					});
		}
	}
	
	public int getMonthNumber();
	
}
