package org.oddjob.events.example;

import org.oddjob.events.InstantEvent;
import org.oddjob.util.Restore;

import java.util.function.Consumer;

public interface FactStore {

	<T> Restore subscribe(String query, Consumer<? super InstantEvent<T>> consumer);
}
