package cc.funkemunky.api.event.system;

import cc.funkemunky.api.Atlas;
import com.google.common.collect.Lists;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class EventManager {
    private static final Map<Listener, List<Method>> registered = new HashMap<>();

    public static void register(Listener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventMethod.class)) {
                List<Method> methods = registered.getOrDefault(listener, new ArrayList<>());
                methods.add(method);

                methods.sort(Comparator.comparingInt(m -> m.getAnnotation(EventMethod.class).priority().getPriority()));
                Collections.reverse(methods);

                registered.put(listener, methods);
            }
        }
    }


    public static void unregister(Listener listener) {
        registered.remove(listener);
    }

    public static Set<Listener> getRegistered() {
        return registered.keySet();
    }

    public static void clearRegistered() {
        registered.clear();
    }

    public static void callEvent(Event event) {
        Atlas.getInstance().getThreadPool().execute(() -> call(event));
    }

    private static void call(Event event) {
        for (Listener listener : registered.keySet()) {
            for (Method method : registered.get(listener)) {
                if (method.getParameterTypes()[0] == event.getClass()) {
                    try {
                        method.invoke(listener, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

