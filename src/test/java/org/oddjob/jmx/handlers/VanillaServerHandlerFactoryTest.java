package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.jmx.client.LogPollable;

import javax.management.MBeanFeatureInfo;
import javax.management.MBeanOperationInfo;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;

public class VanillaServerHandlerFactoryTest extends OjTestCase {

    @Test
    public void testOpInfoForClass() {

        VanillaServerHandlerFactory<LogPollable> test =
				new VanillaServerHandlerFactory<>(LogPollable.class);

        MBeanOperationInfo[] results = test.getMBeanOperationInfo();

        assertEquals(4, results.length);

		Map<String, MBeanOperationInfo> groupByName =
				Arrays.stream(results)
						.collect(Collectors.toMap(
								MBeanFeatureInfo::getName, Function.identity()));

        MBeanOperationInfo urlInfo = groupByName.get("url");

        assertThat(urlInfo.getReturnType(), is(String.class.getName()));

    }

	@Test
	public void testOpInfoForVoidReturn() {

		VanillaServerHandlerFactory<Runnable> test =
				new VanillaServerHandlerFactory<>(Runnable.class);

		MBeanOperationInfo[] results = test.getMBeanOperationInfo();

		assertEquals(1, results.length);

		MBeanOperationInfo runInfo = results[0];

		assertThat(runInfo.getReturnType(), is(Void.TYPE.getName()));

	}
}
