package org.oddjob.beanbus.destinations;

import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.beanbus.AbstractFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
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
public class BusCollect<T> extends AbstractFilter<T, T> implements Resettable {

	private static final Logger logger = LoggerFactory.getLogger(BusCollect.class);

    /**
     * @oddjob.property
     * @oddjob.description The captured beans.
     * @oddjob.required Read only.
     */
    private final ListContainer<T> list = new ListContainer<>();

    private final MapContainer<Object, T> map = new MapContainer<>();

    private volatile Function<? super T, Object> mappingFunction;

    private final AtomicInteger count = new AtomicInteger();

    @Override
    public boolean softReset() {
        return hardReset();
    }

    @Override
    public boolean hardReset() {
        map.items.clear();
        list.items.clear();
        count.set(0);
        return true;
    }

    @Override
    protected T filter(T from) {
        if (mappingFunction == null) {
            list.items.add(from);
        }
		else {
  			map.items.put(mappingFunction.apply(from), from);
        }
		count.incrementAndGet();

        return from;
    }

    public ListContainer<T> getBeans() {
		return list;
    }

	public ListContainer<T> getList() {
		return list;
	}

	public MapContainer<Object, T> getMap() {
		return map;
	}

    public int getCount() {
		return count.get();
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

        private volatile String lastString;

        @Override
        public String toString() {
            if (lastString == null) {
                Iterator<E> it = items.iterator();
                if (!it.hasNext())
                    return "[]";

                StringBuilder sb = new StringBuilder();
                sb.append('[');

                int count = 0;
                for (; ; ) {
                    E e = it.next();
                    sb.append(e);
                    if (++count == 6) {
                        lastString = sb.append("...]").toString();
                        return lastString;
                    }
                    if (!it.hasNext()) {
                        return sb.append(']').toString();
                    }
                    sb.append(',').append(' ');
                }
            }
            return super.toString();
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
    }

    public static class MapContainer<K, E> {

        private final ConcurrentHashMap<K, E> items = new ConcurrentHashMap<>();

        private volatile String lastString;

        @Override
        public String toString() {
            if (lastString == null) {
                Iterator<Map.Entry<K, E>> it = items.entrySet().iterator();
                if (!it.hasNext())
                    return "{}";

                StringBuilder sb = new StringBuilder();
                sb.append('{');

                int count = 0;
                for (; ; ) {
                    Map.Entry<K, E> e = it.next();
                    sb.append(e.getKey());
                    sb.append('=');
                    sb.append(e.getValue());
                    if (++count == 6) {
                        lastString = sb.append("...}").toString();
                        return lastString;
                    }
                    if (!it.hasNext()) {
                        return sb.append('}').toString();
                    }
                    sb.append(',').append(' ');
                }
            }
            return super.toString();
        }

        public Map<K, E> getMap() {
            return new HashMap<>(items);
        }

        public int getSize() {
            return items.size();
        }
    }
}
