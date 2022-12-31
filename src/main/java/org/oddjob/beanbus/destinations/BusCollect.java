package org.oddjob.beanbus.destinations;

import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.beanbus.AbstractFilter;
import org.oddjob.framework.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @param <T> The type of the beans to be collected.
 * @author rob
 * @oddjob.description A component that collects beans in a list. Additionally, this component may
 * be used in the middle of a {@link org.oddjob.beanbus.bus.BasicBusService} so can act as a Wire Tap.
 * @oddjob.example There are many examples elsewhere.
 * <ul>
 * 	<li>{@link Batcher}</li>
 * 	<li>{@link BeanCopy}</li>
 *  <li>{@link BusQueue}</li>
 *  <li>{@link BusLimit}</li>
 * </ul>
 */
public class BusCollect<T> extends AbstractFilter<T, T> implements Resettable, Service {

    private static final Logger logger = LoggerFactory.getLogger(BusCollect.class);

    /**
     * @oddjob.property
     * @oddjob.description The collected items as list container. Conversions exist so that this list
     * property can be used as a list or the values can be accessed using an indexed accessor on the value property.
     * @oddjob.required Read only.
     */
    private final ListContainer<T> list = new ListContainer<>();

    /**
     * @oddjob.property
     * @oddjob.description The collected items as a map container. Conversions exist so that this map
     * property can be used as a map or the values can be accessed using a mapped accessor on the value property.
     * @oddjob.required Read only.
     */
    private final MapContainer<Object, Object> map = new MapContainer<>();

    /**
     * @oddjob.property
     * @oddjob.description A function that will extract a key from an item. If this property is set, items will
     * be available as a map, not a list.
     * @oddjob.required No.
     */
    private volatile Function<? super T, Object> keyMapper;

    /**
     * @oddjob.property
     * @oddjob.description A function that will extract a value from an item to put in the map. If this property
     * is set, but {@link #keyMapper} is not, then it will be silently ignored.
     * @oddjob.required No.
     */
    private volatile Function<? super T, Object> valueMapper;

    /**
     * @oddjob.property
     * @oddjob.description An output stream that items will be written to as strings.
     * @oddjob.required No.
     */
    private volatile OutputStream output;

    /**
     * @oddjob.property
     * @oddjob.description Count of items collected.
     * @oddjob.required R/O.
     */
    private final AtomicInteger count = new AtomicInteger();

    private volatile Strategy<T> strategy;

    @Override
    public boolean softReset() {
        return hardReset();
    }

    @Override
    public boolean hardReset() {
        map.clear();
        list.clear();
        count.set(0);
        return true;
    }

    @Override
    public void start() {

        if (output != null) {
            this.strategy = new OutputStrategy<>(output);
        } else if (keyMapper != null) {
            if (valueMapper == null) {
                this.strategy = new KeyMapperStrategy<>(map, keyMapper);
            } else {
                this.strategy = new KeyValueMapperStrategy<>(map, keyMapper, valueMapper);
            }
        } else {
            strategy = new ListStrategy<>(list);
        }
    }

    @Override
    public void stop() throws FailedToStopException {
        try {
            this.strategy.close();
        } catch (Exception e) {
            throw new FailedToStopException("Failed closing Strategy", e);
        }
    }


    @Override
    protected T filter(T from) {
        strategy.accept(from);

        count.incrementAndGet();

        return from;
    }

    interface Strategy<T> extends Consumer<T>, AutoCloseable {

    }

    static class OutputStrategy<T> implements Strategy<T> {

        private final PrintStream printStream;

        OutputStrategy(OutputStream output) {
            this.printStream = new PrintStream(output);
        }

        @Override
        public void accept(T t) {
            printStream.println(t);
        }

        @Override
        public void close() throws Exception {
            printStream.close();
        }
    }

    static class ListStrategy<T> implements Strategy<T> {

        private final ListContainer<T> listContainer;

        ListStrategy(ListContainer<T> listContainer) {
            this.listContainer = listContainer;
        }

        @Override
        public void accept(T t) {
            listContainer.add(t);
        }

        @Override
        public void close() throws Exception {
            // nothing to do
        }

    }

    static class KeyMapperStrategy<T> implements Strategy<T> {

        private final MapContainer<Object, Object> mapContainer;

        private final Function<? super T, Object> keyMapper;

        KeyMapperStrategy(MapContainer<Object, Object> mapContainer, Function<? super T, Object> keyMapper) {
            this.mapContainer = mapContainer;
            this.keyMapper = keyMapper;
        }


        @Override
        public void accept(T t) {
            mapContainer.put(keyMapper.apply(t), t);
        }

        @Override
        public void close() throws Exception {
            // nothing to do
        }

    }

    static class KeyValueMapperStrategy<T> implements Strategy<T> {

        private final MapContainer<Object, Object> mapContainer;

        private final Function<? super T, Object> keyMapper;

        private final Function<? super T, Object> valueMapper;

        KeyValueMapperStrategy(MapContainer<Object, Object> mapContainer, Function<? super T, Object> keyMapper, Function<? super T, Object> valueMapper) {
            this.mapContainer = mapContainer;
            this.keyMapper = keyMapper;
            this.valueMapper = valueMapper;
        }


        @Override
        public void accept(T t) {
            mapContainer.put(keyMapper.apply(t), valueMapper.apply(t));
        }

        @Override
        public void close() throws Exception {
            // nothing to do
        }

    }


    /**
     * @return A list container.
     * @oddjob.property beans
     * @oddjob.description The same as list. Prefer list, will be deprecated in future versions.
     * @oddjob.required Read only.
     */
    public ListContainer<T> getBeans() {
        return list;
    }

    public ListContainer<T> getList() {
        return list;
    }

    public MapContainer<Object, Object> getMap() {
        return map;
    }

    public int getCount() {
        return count.get();
    }

    public Function<? super T, Object> getKeyMapper() {
        return keyMapper;
    }

    public void setKeyMapper(Function<? super T, Object> keyMapper) {
        this.keyMapper = keyMapper;
    }

    public Function<? super T, Object> getValueMapper() {
        return valueMapper;
    }

    public void setValueMapper(Function<? super T, Object> valueMapper) {
        this.valueMapper = valueMapper;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public static class Conversions implements ConversionProvider {

        @Override
        public void registerWith(ConversionRegistry registry) {

            registry.register(ListContainer.class, List.class, ListContainer::getList);
            registry.register(MapContainer.class, Map.class, MapContainer::getMap);
        }
    }

    public static class ListContainer<E> {

        private final ConcurrentLinkedQueue<E> items = new ConcurrentLinkedQueue<>();

        private final AtomicInteger count = new AtomicInteger();

        private volatile String lastString;

        @Override
        public String toString() {
            if (lastString == null) {
                Iterator<E> it = items.iterator();
                if (!it.hasNext()) {
                    lastString = "[]";
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append('[');

                    int count = 0;
                    for (; ; ) {
                        E e = it.next();
                        if (++count > 5) {
                            lastString = sb.append("...]").toString();
                            break;
                        } else {
                            sb.append(e);
                        }
                        if (!it.hasNext()) {
                            lastString = sb.append(']').toString();
                            break;
                        }
                        sb.append(',').append(' ');
                    }
                }
            }
            return lastString;
        }

        public List<E> getList() {
            return new ArrayList<>(items);
        }

        public int getSize() {
            return items.size();
        }

        public E getValue(int index) {

            Iterator<E> it = items.iterator();
            for (int i = 0; i < index - 1; ++i) {
                if (it.hasNext()) {
                    it.next();
                } else {
                    return null;
                }
            }
            if (it.hasNext()) {
                return it.next();
            } else {
                return null;
            }
        }

        void clear() {
            items.clear();
            count.set(0);
            lastString = null;
        }

        void add(E t) {
            items.add(t);
            if (count.incrementAndGet() <= 6) {
                lastString = null;
            }
        }
    }

    public static class MapContainer<K, E> {

        private final ConcurrentHashMap<K, E> items = new ConcurrentHashMap<>();

        private volatile String lastString;

        @Override
        public String toString() {
            if (lastString == null) {
                Iterator<Map.Entry<K, E>> it = items.entrySet().iterator();
                if (!it.hasNext()) {
                    lastString = "{}";
                } else {

                    StringBuilder sb = new StringBuilder();
                    sb.append('{');

                    int count = 0;
                    for (; ; ) {
                        Map.Entry<K, E> e = it.next();
                        if (++count == 6) {
                            lastString = sb.append("...}").toString();
                            break;
                        } else {
                            sb.append(e.getKey());
                            sb.append('=');
                            sb.append(e.getValue());
                        }
                        if (!it.hasNext()) {
                            lastString = sb.append('}').toString();
                            break;
                        }
                        sb.append(',').append(' ');
                    }
                }
            }
            return lastString;
        }

        public Map<K, E> getMap() {
            return new HashMap<>(items);
        }

        public E getValue(K key) {
            return items.get(key);
        }

        public int getSize() {
            return items.size();
        }

        void clear() {
            items.clear();
            lastString = null;
        }

        public void put(K key, E value) {
            items.put(key, value);
            if (items.size() <= 6) {
                lastString = null;
            }
        }
    }
}
