package com.star.easygenerate.service;

import java.io.StringWriter;
import java.util.Map;

import com.intellij.openapi.components.ServiceManager;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author wangchao
 * @date 2021/03/20
 */
public class VelocityService {
    private static final VelocityEngine VELOCITY_ENGINE;

    static {
        VELOCITY_ENGINE = new VelocityEngine();
        VELOCITY_ENGINE.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VELOCITY_ENGINE.init();
    }

    public static VelocityService getInstance() {
        return ServiceManager.getService(VelocityService.class);
    }

    /**
     * 构建
     *
     * @param name 文件名字
     * @param params 参数
     * @return {@link String}
     */
    public String build(String name, Map<String, Object> params) {
        Template template = VELOCITY_ENGINE.getTemplate(name);
        VelocityContext ctx = new VelocityContext(params);
        StringWriter sw = new StringWriter();
        template.merge(ctx, sw);
        return sw.toString();
    }

}
