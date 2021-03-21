package com.star.easygenerate.action;

import java.io.StringWriter;
import java.util.Properties;

import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author wangchao
 * @date 2021/03/20
 */
public class GenerateUnitTestTemplateActionTest {

    @Test
    public void actionPerformed() throws Exception {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        ve.init();




        Template t = ve.getTemplate("template/unit-test.vm");
        VelocityContext ctx = new VelocityContext();

        ctx.put("name", "velocity");
        ctx.put("whoami", "ccstar");


        StringWriter sw = new StringWriter();


        t.merge(ctx, sw);

        System.out.println(sw.toString());
    }
}