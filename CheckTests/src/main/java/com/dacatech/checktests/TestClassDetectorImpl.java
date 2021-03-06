package com.dacatech.checktests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/8/13 Time: 3:41 PM
 */
public class TestClassDetectorImpl extends TestClassDetector {
    private final Project myProject;
    private static final Logger LOG = Logger.getInstance("#com.dacatech.checktests.TestClassDetectorImpl");
    private Exception myException;

    public TestClassDetectorImpl(final Project project) {
        myProject = project;
    }

    @Override
    public Set<PsiClass> findTestClasses(final List<VirtualFile> virtualFiles, final int levelsToSearch) throws ProcessCanceledException {
        final Set<PsiClass> result = Sets.newHashSet();
        ReadAction.nonBlocking(() -> {
            @Nullable final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
            if (progress != null) {
                progress.setText("Checking for Tests...");
                progress.setIndeterminate(true);
            }
            final LinkedList<PsiElement> referenceSearchElements = Lists.newLinkedList();
            for (final VirtualFile virtualFile : virtualFiles) {
                if (progress != null && progress.isCanceled()) {
                    throw new ProcessCanceledException();
                }
                referenceSearchElements.addAll(getReferenceSearchElements(virtualFile));
            }
            result.addAll(findTestClasses(referenceSearchElements, levelsToSearch));
        })
                .inSmartMode(myProject)
                .submit(AppExecutorUtil.getAppExecutorService());

        if (myException != null) {
            ExceptionUtil.rethrowUnchecked(myException);
        }

        return result;
    }

    Set<PsiElement> getReferenceSearchElements(final VirtualFile virtualFile) {
        final Set<PsiElement> elements = Sets.newHashSet();
        if (virtualFile != null) {
            final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
            final PsiClass[] psiClasses = PsiUtils.getPsiClasses(psiFile);
            if (psiClasses != null) {
                elements.addAll(Arrays.asList(psiClasses));
            } else {
                elements.add(psiFile);
            }
        }
        return elements;
    }

    // VisibleForTesting
    Set<PsiClass> findTestClasses(final LinkedList<PsiElement> psiElementsToSearch, final int levelsToSearch) {
        final Set<PsiClass> testClasses = new HashSet<>();
        int currentSearchLevel = 1;
        int lastIndexForCurrentSearchLevel = psiElementsToSearch.size() - 1;
        for (int idx = 0; idx < psiElementsToSearch.size(); idx++) {
            if (idx > lastIndexForCurrentSearchLevel) {
                currentSearchLevel++;
                lastIndexForCurrentSearchLevel = psiElementsToSearch.size() - 1;
            }
            if (currentSearchLevel > levelsToSearch && levelsToSearch != 0) {
                break;
            }
            final PsiElement psiElementToSearch = psiElementsToSearch.get(idx);
            final List<PsiReference> psiReferences = new ArrayList<>();

            final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(psiElementToSearch.getProject());
            final PsiReference[] references = ReferencesSearch.search(psiElementToSearch, projectScope, false).toArray(
                    new PsiReference[0]);
            psiReferences.addAll(Lists.newArrayList(references));

            for (final PsiReference psiReference : psiReferences) {
                final PsiElement referenceElement = psiReference.getElement();
                final PsiClass referencePsiClass = PsiUtils.getPsiClass(referenceElement);
                if (referencePsiClass != null) {
                    if (!psiElementsToSearch.contains(referencePsiClass)) {
                        psiElementsToSearch.addLast(referencePsiClass);
                    }
                    if (isTestClass(referencePsiClass)) {
                        testClasses.add(referencePsiClass);
                    }
                } else {
                    final PsiFile psiFile = referenceElement.getContainingFile();
                    if (!psiElementsToSearch.contains(psiFile)) {
                        psiElementsToSearch.addLast(psiFile);
                    }
                }
            }
        }
        return testClasses;
    }

    /**
     * Will add the file psiFile to the testClassFile List. if a PsiFile with the same name already exists then it just
     * returns
     *
     * @param newPsiClass the class to add
     */
    private boolean isTestClass(final PsiClass newPsiClass) {
        return JUnitUtil.isTestClass(newPsiClass);
    }
}
