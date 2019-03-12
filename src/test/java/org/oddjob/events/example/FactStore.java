package org.oddjob.events.example;

import org.oddjob.events.EventOf;
import org.oddjob.util.Restore;

import java.util.function.Consumer;

public interface FactStore {

	<T> Restore subscribe(String query, Consumer<? super EventOf<T>> consumer) throws Exception;
}
