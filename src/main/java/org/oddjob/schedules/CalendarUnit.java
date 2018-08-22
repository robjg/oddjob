package org.oddjob.schedules;

/**
 * Encapsulate a Unit of time that can be used
 * for field manipulation with a Calendar.  
 * 
 * @author rob
 *
 */
public class CalendarUnit {

	private final int field;
	
	private final int value;
	
	/**
	 * Constructor.
	 * 
	 * @param field The field that Calendar recognises.
	 * @param value The value of the unit.
	 */
	public CalendarUnit(int field, int value) {
		this.field = field;
		this.value = value;
	}
	
	public int getField() {
		return field;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ", field=" + field + 
				", value=" + value;
	}
}
