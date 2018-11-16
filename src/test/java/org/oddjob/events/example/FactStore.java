package org.oddjob.events.example;

import org.oddjob.util.Restore;

import java.util.function.Consumer;

public interface FactStore {

	<T> Restore subscribe(String query, Consumer<? super T> consumer) throws Exception;
}
