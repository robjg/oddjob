package org.oddjob.jmx.handlers;

import org.junit.jupiter.api.Test;
import org.oddjob.OjTestCase;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;

class OddjobTransportableExceptionTest extends OjTestCase{

	private static final Logger logger = LoggerFactory.getLogger(OddjobTransportableExceptionTest.class);

	@Test
	void testCreate() throws Exception {
		
		Exception e = new Exception("An error occurred!", 
				new Exception("Because of this", 
						new Exception("And this")));
		
		OddjobTransportableException test = 
				new OddjobTransportableException(e);

		OddjobTransportableException copy1 = OddjobTestHelper.copy(test);
		
		logger.info("The exception.", copy1);
		
		assertThat(copy1.toString(), is("java.lang.Exception: An error occurred!"));
		
		Throwable copy2 = copy1.getCause();

		assertThat(copy2.toString(), is("java.lang.Exception: Because of this"));
		
		Throwable copy3 = copy2.getCause();

		assertThat(copy3.toString(), is("java.lang.Exception: And this"));
	}

	@Test
	void testNullMessage() {
		
		Exception e = new Exception();
		
		OddjobTransportableException test = 
				new OddjobTransportableException(e);

		assertNull(test.getMessage());
	}

	@Test
	void asBeanAndBack() {

		Exception e = new Exception("An error occurred!",
				new Exception("Because of this",
						new Exception("And this")));

		OddjobTransportableException test =
				new OddjobTransportableException(e);

		OddjobTransportableException.AsBean asBean = test.toBean();

		OddjobTransportableException copy1 = OddjobTransportableException.fromBean(asBean);

		logger.info("The exception.", copy1);

		assertThat(copy1.toString(), is("java.lang.Exception: An error occurred!"));

		Throwable copy2 = copy1.getCause();

		assertThat(copy2.toString(), is("java.lang.Exception: Because of this"));

		Throwable copy3 = copy2.getCause();

		assertThat(copy3.toString(), is("java.lang.Exception: And this"));
	}
}
