package org.oddjob.events;

import java.util.function.Consumer;

import org.oddjob.util.Restore;

public interface SubscribeNode<T> {

	Restore start(Consumer<? super T> consumer) throws Exception;
}
