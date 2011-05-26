package org.oddjob.jobs;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;

/**
 * @oddjob.description Checks a value for certain criteria. This
 * job is analogous to the unix 'test' command. 
 * <p>
 * This Job will COMPLETE if all checks pass. It will be INCOMPLETE
 * if any fail.
 * <p>
 * The conditional values are converted into the type of the 
 * value before the checks are made. Thus in the example below 
 * if the row count property is an integer, the 1000 is converted
 * into an integer for the comparison.
 * <p>
 * If the value property is not provided the job will be INCOMPLETE.
 * 
 * @oddjob.example
 * 
 * Example text comparisons. All these checks COMPLETE.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/CheckTextExample.xml}
 * 
 * Checks that are INCOMPLETE.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/CheckTextIncompleteExample.xml}
 * 
 * @oddjob.example
 * 
 * Numeric checks. Note that the value must be the numeric value. The
 * operand attributes values are converted to the type of the value before
 * the comparison. If the value was "999" and the lt was "${sequence.current}"
 * this check would not be COMPLETE because the text "999" is greater than 
 * "1000".
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/CheckNumberExample.xml}
 * 
 * @oddjob.example
 * 
 * Check a Property Exists. The secon check will be INCOMPLETE because the
 * prpoerty doesn't exist.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/CheckExistsExample.xml}
 * 
 * @author rob
 *
 */
public class CheckJob 
implements Runnable, Serializable, ArooaSessionAware {
	private static final long serialVersionUID = 2009092700L;
	
	private static final Logger logger = Logger.getLogger(CheckJob.class);
	
	/**
	 * @oddjob.property
	 * @oddjob.description The result of the check.
	 */
	private int result;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value to check.
	 * @oddjob.required No, but the check will fail.
	 */
	private transient Object value;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value must be equal to this. 
	 * @oddjob.required No.
	 */
	private transient ArooaValue eq;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value must be not equal to this. 
	 * @oddjob.required No.
	 */
	private transient ArooaValue ne;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value must be less than this. 
	 * @oddjob.required No.
	 */
	private transient ArooaValue lt;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value must be less than or equals to this. 
	 * @oddjob.required No.
	 */
	private transient ArooaValue le;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value must be greater than this. 
	 * @oddjob.required No.
	 */
	private transient ArooaValue gt;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The value must be greater than or equal to this. 
	 * @oddjob.required No.
	 */
	private transient ArooaValue ge;
	
	private transient ArooaSession session;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The name of this job. Can be any text.
	 * @oddjob.required No.
	 */
	private transient String name;
	
	@SuppressWarnings("unchecked")
	public void run() {
		
		result = 0;
		
		Check[] checks = new Check[] {
				new Check() {
					@Override
					public boolean required() {
						return true;
					}
					@Override
					public boolean check() {
						return value != null;
					}
					@Override
					public String toString() {
						return "Value Exists";
					}
				},
				new Check() {
					@Override
					public boolean required() {
						return eq != null;
					}
					@Override
					public boolean check() {
						return value.equals(convert(eq));						
					}
					@Override
					public String toString() {
						return "" + value + " eq " + eq;
					}
				},
				new Check() {
					@Override
					public boolean required() {
						return ne != null;
					}
					@Override
					public boolean check() {
						return !value.equals(convert(ne));						
					}
					@Override
					public String toString() {
						return "" + value + " ne " + ne;
					}
				},
				new Check() {
					@Override
					public boolean required() {
						return lt != null;
					}
					@SuppressWarnings("rawtypes")
					@Override
					public boolean check() {
						return ((Comparable) value).compareTo(convert(lt)) < 0;						
					}
					@Override
					public String toString() {
						return "" + value + " lt " + lt;
					}
				},
				new Check() {
					@Override
					public boolean required() {
						return le != null;
					}
					@Override
					@SuppressWarnings("rawtypes")
					public boolean check() {
						return ((Comparable) value).compareTo(convert(le)) <= 0;						
					}
					@Override
					public String toString() {
						return "" + value + " le " + le;
					}
				},
				new Check() {
					@Override
					public boolean required() {
						return gt != null;
					}
					@Override
					@SuppressWarnings("rawtypes")
					public boolean check() {
						return ((Comparable) value).compareTo(convert(gt)) > 0;						
					}
					@Override
					public String toString() {
						return "" + value + " gt " + gt;
					}
				},
				new Check() {
					@Override
					public boolean required() {
						return ge != null;
					}
					@Override
					@SuppressWarnings("rawtypes")
					public boolean check() {
						return ge == null || ((Comparable) value).compareTo(convert(ge)) >= 0;						
					}
					@Override
					public String toString() {
						return "" + value + " ge " + ge;
					}
				}
		};
		
		for (Check check : checks) {
			if (!check.required()) {
				continue;
			}
			if (check.check()) {
				logger.info("Check passed: " + check);
			}
			else {
				logger.info("Check failed: " + check);
				result = 1;
				return;
			}
		}
	}

	/**
	 * Encapsulate the a check.
	 */
	interface Check {
		/**
		 * Is a check required?
		 * 
		 * @return true if it is, false if it isn't required.
		 */
		boolean required();
		
		/**
		 * Perform the check.
		 * 
		 * @return true if it passed, false if it didn't.
		 */
		boolean check();
	}
	
	/**
	 * Convert the Right Hand Side of the expression to the type of the
	 * value property.
	 * 
	 * @param rhs
	 * @return
	 */
	Object convert(ArooaValue rhs) {
		
		ArooaConverter converter = session.getTools().getArooaConverter();
		try {
			return converter.convert(rhs, value.getClass());
		} catch (NoConversionAvailableException e) {
			throw new RuntimeException(e);
		} catch (ConversionFailedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@ArooaHidden
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	@ArooaAttribute
	public void setValue(Object value) {
		this.value = value;
	}

	public ArooaValue getEq() {
		return eq;
	}

	@ArooaAttribute
	public void setEq(ArooaValue eq) {
		this.eq = eq;
	}

	public ArooaValue getNe() {
		return ne;
	}

	@ArooaAttribute
	public void setNe(ArooaValue ne) {
		this.ne = ne;
	}

	public ArooaValue getLt() {
		return lt;
	}

	@ArooaAttribute
	public void setLt(ArooaValue lt) {
		this.lt = lt;
	}

	public ArooaValue getGt() {
		return gt;
	}

	@ArooaAttribute
	public void setGt(ArooaValue gt) {
		this.gt = gt;
	}

	public ArooaValue getLe() {
		return le;
	}


	@ArooaAttribute
	public void setLe(ArooaValue le) {
		this.le = le;
	}


	public ArooaValue getGe() {
		return ge;
	}


	@ArooaAttribute
	public void setGe(ArooaValue ge) {
		this.ge = ge;
	}


	public int getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		return name == null ? CheckJob.class.getSimpleName() : name;
	}
}
