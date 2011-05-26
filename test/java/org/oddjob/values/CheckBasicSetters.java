/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.values;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.framework.SimpleJob;

/**
 *
 * @author Rob Gordon.
 */
public class CheckBasicSetters extends SimpleJob {

    boolean checkBoolean;
    byte checkByte;
    char checkChar;
    Date checkDate;
    double checkDouble;
    float checkFloat;
    int checkInt;
    long checkLong;
    short checkShort;
    String checkString;
    
    public int execute() {
		if (checkBoolean != true) {
		    throw new IllegalStateException("boolean wrong.");
		}
		if (checkByte != 127) {
		    throw new IllegalStateException("byte wrong.");
		}
		if (checkChar != 'a') {
		    throw new IllegalStateException("char wrong.");
		}
		if (!new SimpleDateFormat("dd-MMM-yy").format(checkDate)
		        .equals("25-Dec-05")) {
		    throw new IllegalStateException("date wrong.");
		}
		if (checkDouble != 9E99) {
		    throw new IllegalStateException("double wrong.");
		}
		if (checkFloat != 1.23F) {
		    throw new IllegalStateException("float wrong.");
		}
		if (checkInt != 1234567) {
		    throw new IllegalStateException("int wrong.");
		}
		if (checkLong != 2345678) {
		    throw new IllegalStateException("int wrong.");
		}
		if (checkShort !=123) {
		    throw new IllegalStateException("short wrong.");
		}
		if (!checkString.equals("hello")) {
		    throw new IllegalStateException("string wrong.");
		}
		return 0;
    }
    public void setCheckBoolean(boolean checkBoolean) {
        this.checkBoolean = checkBoolean;
    }
    public void setCheckByte(byte checkByte) {
        this.checkByte = checkByte;
    }
    public void setCheckChar(char checkChar) {
        this.checkChar = checkChar;
    }
    @ArooaAttribute
    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }
    public void setCheckDouble(double checkDouble) {
        this.checkDouble = checkDouble;
    }
    public void setCheckFloat(float checkFloat) {
        this.checkFloat = checkFloat;
    }
    public void setCheckInt(int checkInt) {
        this.checkInt = checkInt;
    }
    public void setCheckLong(long checkLong) {
        this.checkLong = checkLong;
    }
    public void setCheckShort(short checkShort) {
        this.checkShort = checkShort;
    }
    public void setCheckString(String checkString) {
        this.checkString = checkString;
    }
}
