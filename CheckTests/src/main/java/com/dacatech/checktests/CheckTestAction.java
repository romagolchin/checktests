package com.dacatech.checktests;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ScrollPaneFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA. User: darata Date: 2/14/13 Time: 12:30 PM
 */
public class CheckTestAction extends AnAction {

    final static String MESSAGE_DISPOSED = "Check for Tests can't be performed while the project is disposed doing other work";
    final static String TITLE_DISPOSED = "Check For Tests Is not Possible Right Now";
    final static String MESSAGE_DUMB = "Check for Tests can't be performed while IntelliJ IDEA updates the indices in background.\n"
            + "You can commit the changes without running inspections, or you can wait until indices are built.";
    final static String TITLE_DUMB = "Check for Tests is not possible right now";

    public void actionPerformed(AnActionEvent event) throws IllegalStateException {
        final DataContext dataContext = event.getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            throw new IllegalStateException("project is null");
        }

        if (project.isDisposed()) {
            showMessageDialog(project, MESSAGE_DISPOSED, TITLE_DISPOSED);
            return;
        }

        if (projectIsDumb(project)) {
            showMessageDialog(project, MESSAGE_DUMB, TITLE_DUMB);
            return;
        }

        checkTests(event, project);
    }

    // VisibleForTesting
    void checkTests(final AnActionEvent event, final Project project) {
        final VirtualFile[] virtualFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFiles != null && virtualFiles.length > 0) {
            final Set<PsiClass> testClasses = getTestClasses(project, virtualFiles);
            if(testClasses != null) {
                showTestListDialog(project, Lists.newArrayList(testClasses));
            }
        }
    }

    // VisibleForTesting
    Set<PsiClass> getTestClasses(Project project, VirtualFile[] virtualFiles) {
        final CheckTestsConfiguration settings = CheckTestsConfiguration.getInstance(project);
        return TestClassDetector.getInstance(project).findTestClasses(Lists.newArrayList(virtualFiles),
                settings.LEVELS_TO_CHECK_FOR_TESTS);
    }

    // VisibleForTesting
    boolean projectIsDumb(Project project) {
        return DumbService.getInstance(project).isDumb();
    }

    // VisibleForTesting
    void showMessageDialog(final Project project, final String message, final String title) {
        Messages.showMessageDialog(project, message, title, null);
    }

    void showTestListDialog(final Project project, final List<PsiClass> testClasses) {
        showDialog(project, testClasses);
    }

    // VisibleForTesting
    static void showDialog(final Project project, final List<PsiClass> testClasses) {
        final DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setTitle("CheckTests Results");
        final JTextArea textArea = new JTextArea(10, 50);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        if (testClasses.size() > 0) {
            final List<String> lines = new ArrayList<>();
            lines.add("Found " + testClasses.size() + " tests, would you like to run them?");
            for (final PsiClass testClass : testClasses) {
                lines.add(testClass.getName());
            }
            textArea.setText(StringUtil.join(lines, "\n"));
            final Runnable runTestsRunnable = () -> {
                dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                TestRunner.runTest(project, testClasses);
            };
            dialogBuilder.setOkOperation(runTestsRunnable);
        } else {
            textArea.setText("Found no tests, maybe you should create one");
        }
        dialogBuilder.setCenterPanel(ScrollPaneFactory.createScrollPane(textArea));
        dialogBuilder.show();
    }
}
