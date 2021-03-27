package com.star.easygenerate.dialog;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.intellij.refactoring.ui.MemberSelectionTable;
import com.intellij.refactoring.ui.PackageNameReferenceEditorCombo;
import com.intellij.refactoring.util.RefactoringMessageUtil;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.testIntegration.TestFramework;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.RecentsManager;
import com.intellij.ui.ReferenceEditorComboWithBrowseButton;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

/**
 * 创建单元测试对话框
 *
 * @author wangchao
 * @date 2021/03/20
 */
public class CreateUnitTestDialog extends DialogWrapper {
    private static final Logger LOGGER = Logger.getInstance(CreateUnitTestDialog.class);

    private static final String RECENTS_KEY = "CreateTestDialog.RecentsKey";
    private static final String SHOW_INHERITED_MEMBERS_PROPERTY = CreateUnitTestDialog.class.getName() + ".includeInheritedMembers";

    private final Project myProject;
    private final PsiClass myTargetClass;
    private final PsiPackage myTargetPackage;
    private final Module myTargetModule;

    protected PsiDirectory myTargetDirectory;

    private EditorTextField myTargetClassNameField;
    private ReferenceEditorComboWithBrowseButton myTargetPackageField;
    private final JCheckBox myGenerateBeforeBox = new JCheckBox(CodeInsightBundle.message("intention.create.test.dialog.setUp"));
    private final JCheckBox myGenerateAfterBox = new JCheckBox(CodeInsightBundle.message("intention.create.test.dialog.tearDown"));
    private final JCheckBox myShowInheritedMethodsBox = new JCheckBox(
        CodeInsightBundle.message("intention.create.test.dialog.show.inherited"));
    private final MemberSelectionTable myMethodsTable = new MemberSelectionTable(Collections.emptyList(), null);

    public CreateUnitTestDialog(@NotNull Project project,
        @NotNull String title,
        PsiClass targetClass,
        PsiPackage targetPackage,
        Module targetModule) {
        super(project, true);
        myProject = project;

        myTargetClass = targetClass;
        myTargetPackage = targetPackage;
        myTargetModule = targetModule;

        setTitle(title);
        init();
    }

    protected String suggestTestClassName(PsiClass targetClass) {
        JavaCodeStyleSettings customSettings = JavaCodeStyleSettings.getInstance(targetClass.getContainingFile());
        String prefix = customSettings.TEST_NAME_PREFIX;
        String suffix = "UnitTest";
        return prefix + targetClass.getName() + suffix;
    }

    private void updateMethodsTable() {
        List<MemberInfo> methods = extractClassMethods(
            myTargetClass, myShowInheritedMethodsBox.isSelected());

        Set<PsiMember> selectedMethods = new HashSet<>();
        for (MemberInfo each : myMethodsTable.getSelectedMemberInfos()) {
            selectedMethods.add(each.getMember());
        }
        for (MemberInfo each : methods) {
            each.setChecked(selectedMethods.contains(each.getMember()));
        }

        myMethodsTable.setMemberInfos(methods);
    }

    private List<MemberInfo> extractClassMethods(PsiClass clazz, boolean includeInherited) {
        List<MemberInfo> result = new ArrayList<>();
        Set<PsiClass> classes;
        if (includeInherited) {
            classes = InheritanceUtil.getSuperClasses(clazz);
            classes.add(clazz);
        } else {
            classes = Collections.singleton(clazz);
        }
        for (PsiClass aClass : classes) {
            if (CommonClassNames.JAVA_LANG_OBJECT.equals(aClass.getQualifiedName())) {
                continue;
            }
            MemberInfo.extractClassMembers(aClass, result, member -> {
                if (!(member instanceof PsiMethod)) {
                    return false;
                }
                return !member.hasModifierProperty(PsiModifier.ABSTRACT);
            }, false);
        }

        return result;
    }

    private void restoreShowInheritedMembersStatus() {
        myShowInheritedMethodsBox.setSelected(getProperties().getBoolean(SHOW_INHERITED_MEMBERS_PROPERTY));
    }

    private void saveShowInheritedMembersStatus() {
        getProperties().setValue(SHOW_INHERITED_MEMBERS_PROPERTY, myShowInheritedMethodsBox.isSelected());
    }

    private PropertiesComponent getProperties() {
        return PropertiesComponent.getInstance(myProject);
    }

    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[] {getOKAction(), getCancelAction(), getHelpAction()};
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myTargetClassNameField;
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints constr = new GridBagConstraints();

        constr.fill = GridBagConstraints.HORIZONTAL;
        constr.anchor = GridBagConstraints.WEST;

        int gridy = 1;

        constr.gridx = 1;
        constr.weightx = 1;
        constr.gridwidth = GridBagConstraints.REMAINDER;

        constr.gridheight = 1;

        constr.insets = insets(6);
        constr.gridy = gridy++;
        constr.gridx = 0;
        constr.weightx = 0;
        constr.gridwidth = 1;
        panel.add(new JLabel(CodeInsightBundle.message("intention.create.test.dialog.class.name")), constr);

        myTargetClassNameField = new EditorTextField(suggestTestClassName(myTargetClass));
        myTargetClassNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent e) {
                getOKAction().setEnabled(PsiNameHelper.getInstance(myProject).isIdentifier(getClassName()));
            }
        });

        constr.gridx = 1;
        constr.weightx = 1;
        panel.add(myTargetClassNameField, constr);

        constr.insets = insets(1);
        constr.gridy = gridy++;
        constr.gridx = 0;
        constr.weightx = 0;
        panel.add(new JLabel(CodeInsightBundle.message("dialog.create.class.destination.package.label")), constr);

        constr.gridx = 1;
        constr.weightx = 1;

        String targetPackageName = myTargetPackage != null ? myTargetPackage.getQualifiedName() : "";
        myTargetPackageField = new PackageNameReferenceEditorCombo(targetPackageName, myProject, RECENTS_KEY,
            CodeInsightBundle.message("dialog.create.class.package.chooser.title"));

        new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                myTargetPackageField.getButton().doClick();
            }
        }.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK)),
            myTargetPackageField.getChildComponent());
        JPanel targetPackagePanel = new JPanel(new BorderLayout());
        targetPackagePanel.add(myTargetPackageField, BorderLayout.CENTER);
        panel.add(targetPackagePanel, constr);

        constr.insets = insets(6);
        constr.gridy = gridy++;
        constr.gridx = 0;
        constr.weightx = 0;
        panel.add(new JLabel(CodeInsightBundle.message("intention.create.test.dialog.generate")), constr);

        constr.gridx = 1;
        constr.weightx = 1;
        panel.add(myGenerateBeforeBox, constr);

        constr.insets = insets(1);
        constr.gridy = gridy++;
        panel.add(myGenerateAfterBox, constr);

        constr.insets = insets(6);
        constr.gridy = gridy++;
        constr.gridx = 0;
        constr.weightx = 0;
        final JLabel membersLabel = new JLabel(CodeInsightBundle.message("intention.create.test.dialog.select.methods"));
        membersLabel.setLabelFor(myMethodsTable);
        panel.add(membersLabel, constr);

        constr.gridx = 1;
        constr.weightx = 1;
        panel.add(myShowInheritedMethodsBox, constr);

        constr.insets = insets(1, 8);
        constr.gridy = gridy++;
        constr.gridx = 0;
        constr.gridwidth = GridBagConstraints.REMAINDER;
        constr.fill = GridBagConstraints.BOTH;
        constr.weighty = 1;
        panel.add(ScrollPaneFactory.createScrollPane(myMethodsTable), constr);

        final List<TestFramework> descriptors = new SmartList<>(TestFramework.EXTENSION_NAME.getExtensionList());
        descriptors.sort((d1, d2) -> Comparing.compare(d1.getName(), d2.getName()));

        myShowInheritedMethodsBox.addActionListener(e -> updateMethodsTable());
        restoreShowInheritedMembersStatus();
        updateMethodsTable();
        return panel;
    }

    private static Insets insets(int top) {
        return insets(top, 0);
    }

    private static Insets insets(int top, int bottom) {
        return JBUI.insets(top, 8, bottom, 8);
    }

    public String getClassName() {
        return myTargetClassNameField.getText();
    }

    public PsiClass getTargetClass() {
        return myTargetClass;
    }

    public PsiDirectory getTargetDirectory() {
        return myTargetDirectory;
    }

    public Collection<MemberInfo> getSelectedMethods() {
        return myMethodsTable.getSelectedMemberInfos();
    }

    public boolean shouldGeneratedAfter() {
        return myGenerateAfterBox.isSelected();
    }

    public boolean shouldGeneratedBefore() {
        return myGenerateBeforeBox.isSelected();
    }

    @Override
    protected void doOKAction() {
        RecentsManager.getInstance(myProject).registerRecentEntry(RECENTS_KEY, myTargetPackageField.getText());

        String errorMessage = null;
        try {
            myTargetDirectory = selectTargetDirectory();
            if (myTargetDirectory == null) {
                return;
            }
        } catch (IncorrectOperationException e) {
            errorMessage = e.getMessage();
        }

        if (errorMessage == null) {
            try {
                errorMessage = checkCanCreateClass();
            } catch (IncorrectOperationException e) {
                errorMessage = e.getMessage();
            }
        }

        if (errorMessage != null) {
            final int result = Messages.showOkCancelDialog(myProject, errorMessage + ". Update existing class?",
                CommonBundle.getErrorTitle(), Messages.OK_BUTTON, Messages.CANCEL_BUTTON, Messages.getErrorIcon());
            if (result == Messages.CANCEL) {
                return;
            }
        }

        saveShowInheritedMembersStatus();
        super.doOKAction();
    }

    protected String checkCanCreateClass() {
        return RefactoringMessageUtil.checkCanCreateClass(myTargetDirectory, getClassName());
    }

    @Nullable
    private PsiDirectory selectTargetDirectory() throws IncorrectOperationException {
        final String packageName = getPackageName();
        final PackageWrapper targetPackage = new PackageWrapper(PsiManager.getInstance(myProject), packageName);

        final VirtualFile selectedRoot = ReadAction.compute(() -> {
            final List<VirtualFile> testFolders = computeTestRoots(myTargetModule);
            List<VirtualFile> roots;
            if (testFolders.isEmpty()) {
                roots = new ArrayList<>();
                List<String> urls = computeSuitableTestRootUrls(myTargetModule);
                for (String url : urls) {
                    try {
                        ContainerUtil.addIfNotNull(roots, VfsUtil.createDirectories(VfsUtilCore.urlToPath(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (roots.isEmpty()) {
                    JavaProjectRootsUtil.collectSuitableDestinationSourceRoots(myTargetModule, roots);
                }
                if (roots.isEmpty()) {
                    return null;
                }
            } else {
                roots = new ArrayList<>(testFolders);
            }

            if (roots.size() == 1) {
                return roots.get(0);
            } else {
                PsiDirectory defaultDir = chooseDefaultDirectory(targetPackage.getDirectories(), roots);
                return MoveClassesOrPackagesUtil.chooseSourceRoot(targetPackage, roots, defaultDir);
            }
        });

        if (selectedRoot == null) {
            return null;
        }

        return WriteCommandAction.writeCommandAction(myProject)
            .withName(CodeInsightBundle.message("create.directory.command"))
            .compute(() -> RefactoringUtil.createPackageDirectoryInSourceRoot(targetPackage, selectedRoot));
    }

    @Nullable
    private PsiDirectory chooseDefaultDirectory(PsiDirectory[] directories, List<VirtualFile> roots) {
        List<PsiDirectory> dirs = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(myProject);
        for (VirtualFile file : ModuleRootManager.getInstance(myTargetModule).getSourceRoots(JavaSourceRootType.TEST_SOURCE)) {
            final PsiDirectory dir = psiManager.findDirectory(file);
            if (dir != null) {
                dirs.add(dir);
            }
        }
        if (!dirs.isEmpty()) {
            for (PsiDirectory dir : dirs) {
                final String dirName = dir.getVirtualFile().getPath();
                if (dirName.contains("generated")) {
                    continue;
                }
                return dir;
            }
            return dirs.get(0);
        }
        for (PsiDirectory dir : directories) {
            final VirtualFile file = dir.getVirtualFile();
            for (VirtualFile root : roots) {
                if (VfsUtilCore.isAncestor(root, file, false)) {
                    final PsiDirectory rootDir = psiManager.findDirectory(root);
                    if (rootDir != null) {
                        return rootDir;
                    }
                }
            }
        }
        return ModuleManager.getInstance(myProject)
            .getModuleDependentModules(myTargetModule)
            .stream().flatMap(module -> ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.TEST_SOURCE).stream())
            .map(psiManager::findDirectory).findFirst().orElse(null);
    }

    private String getPackageName() {
        String name = myTargetPackageField.getText();
        return name != null ? name.trim() : "";
    }

    @Override
    protected void doHelpAction() {
        Desktop dp = Desktop.getDesktop();
        if (dp.isSupported(Desktop.Action.BROWSE)) {
            try {
                dp.browse(URI.create("https://github.com/starcwang/easy_generate"));
            } catch (IOException e) {
                LOGGER.error("open url error: https://github.com/starcwang/easy_generate");
            }
        }
    }

    private static List<VirtualFile> computeTestRoots(@NotNull Module mainModule) {
        if (!computeSuitableTestRootUrls(mainModule).isEmpty()) {
            //create test in the same module, if the test source folder doesn't exist yet it will be created
            return suitableTestSourceFolders(mainModule)
                .map(SourceFolder::getFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        //suggest to choose from all dependencies modules
        final HashSet<Module> modules = new HashSet<>();
        ModuleUtilCore.collectModulesDependsOn(mainModule, modules);
        return modules.stream()
            .flatMap(CreateUnitTestDialog::suitableTestSourceFolders)
            .map(SourceFolder::getFile)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static Stream<SourceFolder> suitableTestSourceFolders(@NotNull Module module) {
        Predicate<SourceFolder> forGeneratedSources = JavaProjectRootsUtil::isForGeneratedSources;
        return Arrays.stream(ModuleRootManager.getInstance(module).getContentEntries())
            .flatMap(entry -> entry.getSourceFolders(JavaSourceRootType.TEST_SOURCE).stream())
            .filter(forGeneratedSources.negate());
    }

    private static List<String> computeSuitableTestRootUrls(@NotNull Module module) {
        return suitableTestSourceFolders(module).map(SourceFolder::getUrl).collect(Collectors.toList());
    }
}