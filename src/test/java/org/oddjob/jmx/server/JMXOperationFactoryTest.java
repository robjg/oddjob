package org.oddjob.jmx.server;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.logging.LogEnabled;

import javax.management.MBeanOperationInfo;

public class JMXOperationFactoryTest extends OjTestCase {

    @Test
    public void testNoArgsOpInfo() throws SecurityException, NoSuchMethodException {

        JMXOperationFactory test = new JMXOperationFactory(LogEnabled.class);

        RemoteOperation<?> expected = new OperationInfoOperation(
                new MBeanOperationInfo("",
                        LogEnabled.class.getMethod("loggerName")));

        assertEquals(expected, test.operationFor(
                LogEnabled.class.getMethod("loggerName"),
                MBeanOperationInfo.INFO));

        assertEquals(expected, test.operationFor(
                LogEnabled.class.getMethod("loggerName"),
                "Get's Log", MBeanOperationInfo.INFO));

        assertEquals(expected, test.operationFor(
                "loggerName",
                MBeanOperationInfo.INFO));

        assertEquals(expected, test.operationFor(
                "loggerName",
                "Get's Log", MBeanOperationInfo.INFO));
    }

    @Test
    public void testVoidReturnType() {

    	JMXOperationFactory test = new JMXOperationFactory(Runnable.class);

    	JMXOperation<Void> op =
				test.operationFor("run", MBeanOperationInfo.INFO);

    	MBeanOperationInfo opInfo = op.getOpInfo();

		String returnType = opInfo.getReturnType();

		assertThat(returnType, Matchers.is(Void.TYPE.getName()));

	}
}
