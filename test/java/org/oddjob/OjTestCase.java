package org.oddjob;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;

public class OjTestCase extends Assert {

	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }

}
