package com.example.demo.listener;

import com.example.demo.config.AppKeys;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@WebListener
public class SessionLifecycleListener implements HttpSessionListener {
    private static final Logger LOG = Logger.getLogger(SessionLifecycleListener.class.getName());

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        int count = counter(event).incrementAndGet();
        LOG.info(() -> "Session 已创建，当前会话数：" + count);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        AtomicInteger counter = counter(event);
        int count = counter.updateAndGet(value -> Math.max(0, value - 1));
        LOG.info(() -> "Session 已销毁，当前会话数：" + count);
    }

    private AtomicInteger counter(HttpSessionEvent event) {
        return (AtomicInteger) event.getSession().getServletContext().getAttribute(AppKeys.ACTIVE_SESSIONS);
    }
}
