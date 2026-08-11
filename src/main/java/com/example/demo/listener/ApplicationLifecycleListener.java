package com.example.demo.listener;

import com.example.demo.config.AppKeys;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
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
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
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
        LOG.info("应用即将停止，Servlet destroy 方法随后完成资源回收");
    }
}
