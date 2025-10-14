package org.oddjob.script;

import javax.script.Bindings;
import java.util.*;

/**
 * Combine Bindings. Used in Oddjob.
 */
public class CompositeBindings implements Bindings {

    private final List<Bindings> bindings;

    private CompositeBindings(List<Bindings> bindings) {
        this.bindings = bindings;
    }

    public static Bindings of(List<Bindings> bindings) {

        return new CompositeBindings(new ArrayList<>(bindings));
    }

    public static Bindings of(Bindings... bindings) {

        return of(List.of(bindings));
    }

    @Override
    public Object put(String name, Object value) {
        throw new UnsupportedOperationException("Read Only");
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        throw new UnsupportedOperationException("Read Only");
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public Object get(Object key) {
        for (Bindings bindings : this.bindings) {
            Object value = bindings.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Read Only");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read Only");
    }

    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<Object> values() {
        return Collections.emptySet();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.emptySet();
    }

}
