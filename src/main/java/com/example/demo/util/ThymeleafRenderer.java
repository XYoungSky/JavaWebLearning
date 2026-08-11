package com.example.demo.util;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ThymeleafRenderer {
    private static final TemplateEngine ENGINE = createEngine();

    private ThymeleafRenderer() {}

    private static TemplateEngine createEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    public static void render(String template, Map<String, Object> variables, Writer writer) {
        Context context = new Context();
        context.setVariables(variables);
        ENGINE.process(template, context, writer);
    }
}
