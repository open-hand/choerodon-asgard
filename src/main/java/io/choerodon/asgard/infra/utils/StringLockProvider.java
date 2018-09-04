package io.choerodon.asgard.infra.utils;

import org.springframework.stereotype.Component;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

@Component
public class StringLockProvider {

    private final Map<Mutex, WeakReference<Mutex>> mutexMap = new WeakHashMap<>();

    public Mutex getMutex(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        Mutex key = new MutexImpl(id);
        synchronized (mutexMap) {
            return mutexMap.computeIfAbsent(key, WeakReference::new).get();
        }
    }

    public interface Mutex {
    }

    private static class MutexImpl implements Mutex {
        private final String code;

        private MutexImpl(String id) {
            this.code = id;
        }

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (this.getClass() == o.getClass()) {
                return this.code.equals(o.toString());
            }
            return false;
        }

        public int hashCode() {
            return code.hashCode();
        }

        public String toString() {
            return code;
        }
    }

}
