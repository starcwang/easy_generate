package com.star.easygenerate.action;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ClassFileViewProvider;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.testIntegration.createTest.CreateTestAction;
import com.intellij.testIntegration.createTest.CreateTestDialog;
import com.intellij.util.ResourceUtil;
import com.star.easygenerate.dialog.CreateUnitTestDialog;
import org.jetbrains.jps.model.java.JavaSourceRootType;

/**
 * @author wangchao
 * @date 2021/03/20
 */
public class GenerateUnitTestTemplateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile)psiFile;
        PsiDirectory srcDir = psiJavaFile.getContainingDirectory();
        PsiPackage srcPackage = JavaDirectoryService.getInstance().getPackage(srcDir);
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(psiJavaFile);

        CreateUnitTestDialog dialog = new CreateUnitTestDialog(project, "xxxxxxx", psiJavaFile.getClasses()[0], srcPackage, srcModule);
        if (!dialog.showAndGet()) {
            return;
        }
        PsiDirectory tgtDir = dialog.getTargetDirectory();

        psiJavaFile.getClasses();

        Map<String, Object> params = Maps.newHashMap();
        params.put("user", "cc");
        params.put("className", psiJavaFile.getClasses()[0].getName());
        params.put("testClassName", dialog.getClassName());

        try {
            String text = ResourceUtil.loadText(ResourceUtil.getResource(this.getClass().getClassLoader(), "template", "unit-test.vm"));

            CustomFileTemplate template = new CustomFileTemplate("java", "java");
            template.setText(text);

            FileTemplateUtil.createFromTemplate(template, dialog.getClassName(), params, tgtDir, null);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //
        //CommandProcessor.getInstance().executeCommand(project, () -> {
        //    final TestGenerator generator = TestGenerators.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
        //    DumbService.getInstance(project).withAlternativeResolveEnabled(() -> generator.generateTest(project, dialog));
        //}, CodeInsightBundle.message("intention.create.test"), this);

        ModuleUtilCore.findModuleForFile(e.getData(CommonDataKeys.VIRTUAL_FILE), project);
        Module testModule = CreateTestAction.suggestModuleForTests(project, srcModule);
        PsiManager psiManager = PsiManager.getInstance(project);
        // 找到了文件夹
        List<VirtualFile> list = ModuleRootManager.getInstance(testModule).getSourceRoots(JavaSourceRootType.TEST_SOURCE);
        PsiDirectory psiDirectory = ModuleManager.getInstance(project)
            .getModuleDependentModules(testModule)
            .stream().flatMap(module -> ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.TEST_SOURCE).stream())
            .map(root -> psiManager.findDirectory(root)).findFirst().orElse(null);
        System.out.println(psiDirectory);

        final CreateTestDialog d = new CreateTestDialog(project, "tannnnn", psiJavaFile.getClasses()[0], srcPackage, srcModule);

        if (!d.showAndGet()) {
            return;
        }
    }

}
