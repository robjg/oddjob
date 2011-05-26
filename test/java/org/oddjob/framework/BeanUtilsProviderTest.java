/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;

public class BeanUtilsProviderTest extends TestCase {

	public static class DateBean {
		Date date; 
		
		@ArooaAttribute
		public void setDate(Date date) {
			this.date = date;
		}
		public Date getDate() {
			return date;
		}
	}
	
	public void testInOddjob() throws ParseException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='d' class='" + DateBean.class.getName() + 
			"' date='2005-12-25 13:53'/>" +
			" </job>" +
			"</oddjob>";
			
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		DateBean bean = (DateBean) new OddjobLookup(oj).lookup("d");
		assertEquals(DateHelper.parseDateTime("2005-12-25 13:53"), bean.getDate());
	}
}
