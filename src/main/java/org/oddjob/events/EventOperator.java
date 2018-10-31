package org.oddjob.events;

import org.oddjob.util.Restore;

import java.util.List;
import java.util.function.Consumer;

public interface EventOperator<T> {

    Restore start(List<T> previous,
                         List<? extends EventSource<? extends T>> nodes,
                         Consumer<? super List<T>> results) throws Exception;

}
