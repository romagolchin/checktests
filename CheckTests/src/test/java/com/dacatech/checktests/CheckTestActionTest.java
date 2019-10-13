package com.dacatech.checktests;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class CheckTestActionTest {
    private CheckTestAction checkTestAction;

    @Before
    public void testCheckTestAction() {
        checkTestAction = new CheckTestAction();
    }

    @Test(expected = IllegalStateException.class)
    public void testActionPerformedProjectIsNull() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final DataContext dataContext = mock(DataContext.class);
        when(anActionEvent.getDataContext()).thenReturn(dataContext);
        when(dataContext.getData(anyString())).thenReturn(null);
        checkTestAction.actionPerformed(anActionEvent);
    }

    @Test
    public void testActionPerformedProjectIsDisposed() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final DataContext dataContext = mock(DataContext.class);
        when(anActionEvent.getDataContext()).thenReturn(dataContext);
        final Project project = mock(Project.class);
        when(dataContext.getData(anyString())).thenReturn(project);
        when(project.isDisposed()).thenReturn(true);
        final CheckTestAction checkTestActionSpy = spy(checkTestAction);
        doNothing().when(checkTestActionSpy)
                .showMessageDialog(any(Project.class), anyString(), anyString());
        checkTestActionSpy.actionPerformed(anActionEvent);
        verify(checkTestActionSpy).showMessageDialog(project, CheckTestAction.MESSAGE_DISPOSED,
                CheckTestAction.TITLE_DISPOSED);
        verify(checkTestActionSpy, never()).checkTests(anActionEvent, project);
    }

    @Test
    public void testActionPerformedProjectIsDumb() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final DataContext dataContext = mock(DataContext.class);
        when(anActionEvent.getDataContext()).thenReturn(dataContext);
        final Project project = mock(Project.class);
        when(dataContext.getData(anyString())).thenReturn(project);
        when(project.isDisposed()).thenReturn(false);
        final CheckTestAction checkTestActionSpy = spy(checkTestAction);
        doNothing().when(checkTestActionSpy)
                .showMessageDialog(any(Project.class), anyString(), anyString());
        doReturn(true).when(checkTestActionSpy).projectIsDumb(project);
        checkTestActionSpy.actionPerformed(anActionEvent);
        verify(checkTestActionSpy).showMessageDialog(project, CheckTestAction.MESSAGE_DUMB,
                CheckTestAction.TITLE_DUMB);
        verify(checkTestActionSpy, never()).checkTests(anActionEvent, project);
    }

    @Test
    public void testActionPerformed() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final DataContext dataContext = mock(DataContext.class);
        when(anActionEvent.getDataContext()).thenReturn(dataContext);
        final Project project = mock(Project.class);
        when(dataContext.getData(anyString())).thenReturn(project);
        when(project.isDisposed()).thenReturn(false);
        final CheckTestAction checkTestActionSpy = spy(checkTestAction);
        doNothing().when(checkTestActionSpy)
                .showMessageDialog(any(Project.class), anyString(), anyString());
        doReturn(false).when(checkTestActionSpy).projectIsDumb(project);
        doNothing().when(checkTestActionSpy).checkTests(anActionEvent, project);
        checkTestActionSpy.actionPerformed(anActionEvent);
        verify(checkTestActionSpy).checkTests(anActionEvent, project);
    }

    @Test
    public void testCheckForTestsNoFilesSelected() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final Project project = mock(Project.class);
        final CheckTestAction checkTestActionSpy = spy(checkTestAction);
        when(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(null);
        checkTestActionSpy.checkTests(anActionEvent, project);
        verify(checkTestActionSpy, never()).getTestClasses(any(Project.class),
                any(VirtualFile[].class));
        verify(checkTestActionSpy, never()).showTestListDialog(any(Project.class),
                anyList());
    }

    @Test
    public void testCheckForTestsNoTestsFound() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final Project project = mock(Project.class);
        final CheckTestAction checkTestActionSpy = spy(checkTestAction);
        final VirtualFile virtualFile = mock(VirtualFile.class);
        final VirtualFile[] virtualFiles = new VirtualFile[] { virtualFile };
        when(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(virtualFiles);
        doReturn(null).when(checkTestActionSpy).getTestClasses(project, virtualFiles);
        checkTestActionSpy.checkTests(anActionEvent, project);
        verify(checkTestActionSpy, never()).showTestListDialog(any(Project.class),
                anyList());
    }

    @Test
    public void testCheckForTests() {
        final AnActionEvent anActionEvent = mock(AnActionEvent.class);
        final Project project = mock(Project.class);
        final CheckTestAction checkTestActionSpy = spy(checkTestAction);
        final VirtualFile virtualFile = mock(VirtualFile.class);
        final VirtualFile[] virtualFiles = new VirtualFile[] { virtualFile };
        when(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(virtualFiles);
        final Set<PsiClass> testClasses = new HashSet<>();
        testClasses.add(mock(PsiClass.class));
        doReturn(testClasses).when(checkTestActionSpy).getTestClasses(project, virtualFiles);
        doNothing().when(checkTestActionSpy).showTestListDialog(any(Project.class), anyList());
        checkTestActionSpy.checkTests(anActionEvent, project);
        verify(checkTestActionSpy).showTestListDialog(any(Project.class), anyList());
    }
}
