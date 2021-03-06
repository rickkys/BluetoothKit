package com.inuker.bluetooth.library.utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by dingjikerbo on 2016/8/23.
 */
public class ProxyUtils {

    public static <T> T getProxy(Object object) {
        return getProxy(object, null);
    }

    public static <T> T getWeakProxy(T object) {
        if (object == null) {
            return null;
        }
        return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                new WeakProxyInvocationHandler(object));
    }

    public static <T> T getWeakProxy(T object, Class<?> clazz) {
        if (object == null) {
            return null;
        }
        return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                new Class<?>[] { clazz },
                new WeakProxyInvocationHandler(object));
    }

    public static <T> T getProxy(Object object, ProxyHandler handler) {
        List<Class<?>> clazzes = getAllInterfaces(object.getClass());
        return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                clazzes.toArray(new Class<?>[clazzes.size()]),
                new ProxyInvocationHandler(object, handler));
    }

    public static <T> T getProxy(Object object, Class<?> clazz, ProxyHandler handler) {
        return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                new Class<?>[] { clazz },
                new ProxyInvocationHandler(object, handler));
    }

    public static <T> T getProxy(Object object, Class<?>[] clazz, ProxyHandler handler) {
        return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                clazz,
                new ProxyInvocationHandler(object, handler));
    }

    public static class ProxyBulk {
        public Object object;
        public Method method;
        public Object[] args;

        public ProxyBulk(Object object, Method method, Object[] args) {
            this.object = object;
            this.method = method;
            this.args = args;
        }

        private Object safeInvoke() {
            Object result = null;
            try {
                result = method.invoke(object, args);
            } catch (Throwable e) {
                BluetoothLog.e(e);
            }
            return result;
        }

        public static Object safeInvoke(Object obj) {
            return ((ProxyBulk) obj).safeInvoke();
        }
    }

    private static List<Class<?>> getAllInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
        getAllInterfaces(cls, interfacesFound);

        return new ArrayList<Class<?>>(interfacesFound);
    }

    private static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
        while (cls != null) {
            final Class<?>[] interfaces = cls.getInterfaces();

            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(i, interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    public interface ProxyHandler {
        boolean onPreCalled(Object object, Method method, Object[] args);
    }

    public static class ProxyHandlerImpl implements ProxyHandler {

        @Override
        public boolean onPreCalled(Object object, Method method, Object[] args) {
            return true;
        }
    }

    private static class ProxyInvocationHandler implements InvocationHandler, ProxyHandler {

        private Object subject;
        private ProxyHandler handler;

        public ProxyInvocationHandler(Object subject, ProxyHandler handler) {
            this.subject = subject;
            this.handler = handler;
        }

        @Override
        public Object invoke(Object object, Method method, Object[] args)
                throws Throwable {
            // TODO Auto-generated method stub
            Object result = null;

            if (onPreCalled(subject, method, args)) {
                try {
                    result = method.invoke(subject, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        public boolean onPreCalled(Object object, Method method, Object[] args) {
            if (handler != null) {
                try {
                    return handler.onPreCalled(object, method, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    private static class WeakProxyInvocationHandler implements InvocationHandler {

        private WeakReference<Object> refSubject;

        public WeakProxyInvocationHandler(Object subject) {
            this.refSubject = new WeakReference<Object>(subject);
        }

        @Override
        public Object invoke(Object object, Method method, Object[] args) {
            // TODO Auto-generated method stub
            try {
                Object subject = refSubject.get();
                if (subject != null) {
                    return method.invoke(subject, args);
                }
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }
    }
}
