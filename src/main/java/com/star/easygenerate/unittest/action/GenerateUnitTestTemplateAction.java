package com.star.easygenerate.unittest.action;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.util.ResourceUtil;
import com.star.easygenerate.unittest.dialog.CreateUnitTestDialog;
import com.star.easygenerate.unittest.model.TemplateField;
import com.star.easygenerate.unittest.model.TemplateMethod;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangchao
 * @date 2021/03/20
 */
public class GenerateUnitTestTemplateAction extends AnAction {
    private static final Logger LOGGER = Logger.getInstance(GenerateUnitTestTemplateAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {

        // 准备数据
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        if (!(psiFile instanceof PsiJavaFile)) {
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile)psiFile;
        PsiDirectory srcDir = psiJavaFile.getContainingDirectory();
        PsiPackage srcPackage = JavaDirectoryService.getInstance().getPackage(srcDir);
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(psiJavaFile);

        // 展示界面
        CreateUnitTestDialog dialog = new CreateUnitTestDialog(project, "create unit test",
            psiJavaFile.getClasses()[0], srcPackage, srcModule);
        if (!dialog.showAndGet()) {
            return;
        }
        PsiDirectory tgtDir = dialog.getTargetDirectory();

        // 拼参数
        Map<String, Object> params = getParamMap(psiJavaFile, dialog);
        if (params == null) {
            return;
        }

        try {
            createUnitTestFile(dialog.getClassName(), tgtDir, params);
        } catch (Exception exception) {
            LOGGER.error("create template error:", exception);
        }

        // 跳转到类上
        String tgtClassName = ((PsiJavaFileImpl)dialog.getTargetClass().getParent()).getPackageName() + "." + dialog.getClassName();
        PsiClass testClass = JavaPsiFacade.getInstance(project).findClass(tgtClassName, GlobalSearchScope.allScope(project));
        if (testClass == null) {
            return;
        }
        NavigationUtil.activateFileWithPsiElement(testClass, true);
    }

    /**
     * 创建单元测试文件
     *
     * @param className 类名
     * @param tgtDir tgt dir
     * @param params 参数个数
     * @throws Exception 异常
     */
    private void createUnitTestFile(String className, PsiDirectory tgtDir, Map<String, Object> params) throws Exception {
        String text = ResourceUtil.loadText(ResourceUtil.getResource(this.getClass().getClassLoader(), "template", "unit-test.vm"));

        CustomFileTemplate template = new CustomFileTemplate("java", "java");
        template.setText(text);
        VirtualFile exists = tgtDir.getVirtualFile().findChild(className + ".java");
        if (exists != null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    exists.delete(this);
                } catch (IOException ioException) {
                    LOGGER.error("delete file error: " + exists, ioException);
                }
            });
        }
        FileTemplateUtil.createFromTemplate(template, className, params, tgtDir, null);
    }

    /**
     * 得到参数
     *
     * @param psiJavaFile psi java文件
     * @param dialog 对话框
     * @return 参数
     */
    @Nullable
    private Map<String, Object> getParamMap(PsiJavaFile psiJavaFile, CreateUnitTestDialog dialog) {
        // 注入属性
        PsiField[] allFields = psiJavaFile.getClasses()[0].getAllFields();
        List<TemplateField> fields = Lists.newArrayList();
        for (PsiField field : allFields) {
            if (field.getAnnotation("javax.annotation.Resource") != null
                || field.getAnnotation("org.springframework.beans.factory.annotation.Autowired") != null) {
                TemplateField templateField = new TemplateField();
                templateField.setPackageName(field.getType().getCanonicalText());
                templateField.setInstanceName(field.getName());
                templateField.setClassName(field.getType().getPresentableText());
                templateField.setJavaDoc(Optional.ofNullable(field.getDocComment()).map(PsiElement::getText).orElse(""));
                fields.add(templateField);
            }
        }

        // 注入方法
        Collection<MemberInfo> selectedMemberList = dialog.getSelectedMethods();
        List<PsiMethod> methodList = selectedMemberList.stream()
            .map(memberInfo -> (PsiMethod)memberInfo.getMember()).collect(Collectors.toList());
        List<TemplateMethod> methods = Lists.newArrayList();
        for (PsiMethod psiMethod : methodList) {
            TemplateMethod templateMethod = new TemplateMethod();
            if (psiMethod.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)) {
                templateMethod.setScope(PsiModifier.PUBLIC);
            } else if (psiMethod.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)) {
                templateMethod.setScope(PsiModifier.PRIVATE);
            }
            templateMethod.setName(psiMethod.getName());
            templateMethod.setNameUp(StringUtils.substring(psiMethod.getName(), 0, 1).toUpperCase() +
                StringUtils.substring(psiMethod.getName(), 1));
            methods.add(templateMethod);
        }

        psiJavaFile.getClasses();
        String className = psiJavaFile.getClasses()[0].getName();
        if (StringUtils.isBlank(className)) {
            return null;
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("user", System.getProperty("user.name"));
        params.put("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        params.put("className", className);
        params.put("package", ((PsiJavaFileImpl)dialog.getTargetClass().getParent()).getPackageName());
        params.put("instanceName", StringUtils.substring(className, 0, 1).toLowerCase() + StringUtils.substring(className, 1));
        params.put("testClassName", dialog.getClassName());
        params.put("fieldList", fields);
        params.put("methodList", methods);
        params.put("hasBefore", dialog.shouldGeneratedBefore());
        params.put("hasAfter", dialog.shouldGeneratedAfter());
        return params;
    }

}
