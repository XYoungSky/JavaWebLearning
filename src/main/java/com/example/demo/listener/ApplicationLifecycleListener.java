package com.example.demo.listener;

import com.example.demo.config.AppKeys;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.example.demo.config.DatabaseConfig;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(ApplicationLifecycleListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try {
            SqlSessionFactory factory = DatabaseConfig.createFactory(DatabaseConfig.load());
            context.setAttribute(AppKeys.SQL_SESSION_FACTORY, factory);
            context.setAttribute(AppKeys.ACTIVE_SESSIONS, new AtomicInteger());
            context.setAttribute(AppKeys.REQUEST_COUNT, new AtomicLong());

            String directoryName = context.getInitParameter("uploadDirectory");
            Path uploadPath = Path.of(System.getProperty("java.io.tmpdir"), directoryName).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            context.setAttribute(AppKeys.UPLOAD_PATH, uploadPath);
            LOG.info(() -> "应用启动完成，上传目录：" + uploadPath);
        } catch (IOException ex) {
            throw new IllegalStateException("应用初始化失败", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Object factory = event.getServletContext().getAttribute(AppKeys.SQL_SESSION_FACTORY);
        if (factory instanceof SqlSessionFactory sessions
                && sessions.getConfiguration().getEnvironment().getDataSource() instanceof PooledDataSource pool) {
            pool.forceCloseAll();
        }
        LOG.info("应用停止，数据库连接池已关闭");
    }
}
