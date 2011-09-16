package org.oddjob.schedules.units;

import java.util.Arrays;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public interface WeekOfMonth {

	enum Weeks implements WeekOfMonth {
		FIRST {
			@Override
			public int getWeekNumber() {
				return 1;
			}
		},
		SECOND {
			@Override
			public int getWeekNumber() {
				return 2;
			}
		},
		THIRD {
			@Override
			public int getWeekNumber() {
				return 3;
			}
		},
		FOURTH {
			@Override
			public int getWeekNumber() {
				return 4;
			}
		},
		FITH {
			@Override
			public int getWeekNumber() {
				return 5;
			}
		},
		LAST {
			@Override
			public int getWeekNumber() {
				return -1;
			}
		},
		PENULTIMATE {
			@Override
			public int getWeekNumber() {
				return -2;
			}
		},
	}
	
	public static class Number implements WeekOfMonth {
	
		private final int weekNumber;
		
		public Number(int weekNumber) {
			this.weekNumber = weekNumber;
		}
		
		@Override
		public int getWeekNumber() {
			return weekNumber;
		}
		
		@Override
		public String toString() {
			return "" + weekNumber;
		}
	}
	
	public static class Conversions implements ConversionProvider {
		
		@Override
		public void registerWith(ConversionRegistry registry) {
			
			registry.register(String.class, WeekOfMonth.class, 
					new Convertlet<String, WeekOfMonth>() {
						 @Override
						public WeekOfMonth convert(String from)
								throws ConvertletException {
							try {
								final int week = Integer.parseInt(from);								
								return new Number(week);
							}
							catch (NumberFormatException e) {
								try {
									return Weeks.valueOf(from.toUpperCase());
								}
								catch (IllegalArgumentException enumEx) {
									
									throw new ConvertletException("[" + from + 
											"] is not a valid day of month. Valid values are " +
											Arrays.asList(Weeks.values()) + " or an integer.");
								}
							}
						}
					});
		}
	}
	
	public int getWeekNumber();
}
