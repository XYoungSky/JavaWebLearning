package com.example.demo;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public final class TestDoubles {
    private TestDoubles() {}

    @FunctionalInterface
    public interface Call {
        Object invoke(Method method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxy(Class<T> type, Call call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (instance, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "TestDouble<" + type.getSimpleName() + ">";
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == args[0];
                    default -> null;
                };
            }
            Object value = call.invoke(method, args == null ? new Object[0] : args);
            if (value != null || !method.getReturnType().isPrimitive()) return value;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType() == long.class) return 0L;
            if (method.getReturnType() == int.class) return 0;
            return null;
        });
    }

    public static SessionState myBatis(Map<Class<?>, Object> mappers) {
        SessionState state = new SessionState();
        state.session = proxy(SqlSession.class, (method, args) -> {
            switch (method.getName()) {
                case "getMapper" -> { return mappers.get((Class<?>) args[0]); }
                case "commit" -> { state.commits++; return null; }
                case "rollback" -> { state.rollbacks++; return null; }
                case "close" -> { state.closed = true; return null; }
                default -> { return null; }
            }
        });
        state.factory = proxy(SqlSessionFactory.class, (method, args) ->
                method.getName().equals("openSession") ? state.session : null);
        return state;
    }

    public static final class SessionState {
        public SqlSessionFactory factory;
        public SqlSession session;
        public int commits;
        public int rollbacks;
        public boolean closed;
    }
}
