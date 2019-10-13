package com.dacatech.checktests;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created with IntelliJ IDEA.
 * User: darata
 * Date: 3/8/13
 * Time: 9:08 AM
 */
public class PsiUtils {
    public static PsiElement getPsiElement(AnActionEvent event) {
        final PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        final int offset = editor.getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }

    public static PsiClass getPsiClass(PsiElement psiElement) {
        return PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
    }

    public static PsiClass[] getPsiClasses(PsiElement psiElement) {
        if(psiElement instanceof PsiClassOwner) {
            final PsiClassOwner psiClassOwner = (PsiClassOwner) psiElement;
            return psiClassOwner.getClasses();
        }
        return null;
    }
}
