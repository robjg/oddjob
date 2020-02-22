package org.oddjob.values.properties;

import org.junit.Test;

import org.oddjob.OjTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;

public class EnvVarPropertyLookupTest extends OjTestCase {

    @Test
    public void testLookup() {

        Map<String, String> found = new HashMap<>();

        EnvVarPropertyLookup test = new EnvVarPropertyLookup("env");

		Consumer<String> check = var -> Optional.ofNullable(test.lookup(var))
				.ifPresent(res -> found.put(var, res));

		check.accept("env.path");
		check.accept("env.PATH");
		check.accept("env.Path");

		assertThat(found.size() > 0, is(true));
    }
}
