package com.dacatech.checktests;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.testframework.TestSearchScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/8/13 Time: 9:24 AM
 */
public class TestRunner {
    public static void runTest(final Project project, final List<PsiClass> testClasses) {
        final RunManagerEx instanceEx = RunManagerEx.getInstanceEx(project);
        final RunnerAndConfigurationSettings configuration = getConfiguration(instanceEx, testClasses);

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();

        if (executor != null) {
            ExecutionTarget target = ExecutionTargetManager.getActiveTarget(project);
            ExecutionManager.getInstance(project).restartRunProfile(project, executor, target, configuration, null);

            RunManagerEx.getInstanceEx(project).addConfiguration(configuration, false);
            instanceEx.setSelectedConfiguration(configuration);
        }
    }

    @NotNull
    private static RunnerAndConfigurationSettings getConfiguration(final RunManager runManager,
                                                                   final List<PsiClass> testClasses) {
        final ConfigurationType type = getConfigurationType();
        final RunnerAndConfigurationSettingsImpl runnerAndConfigurationSettings = (RunnerAndConfigurationSettingsImpl) runManager
                .createConfiguration("CheckTests", type.getConfigurationFactories()[0]);
        final JUnitConfiguration conf = (JUnitConfiguration) runnerAndConfigurationSettings.getConfiguration();
        conf.bePatternConfiguration(testClasses, null);
        final JUnitConfiguration.Data data = conf.getPersistentData();
        data.setScope(TestSearchScope.WHOLE_PROJECT);
        return runnerAndConfigurationSettings;
    }

    private static ConfigurationType getConfigurationType() {
        ConfigurationType[] types = ConfigurationType.CONFIGURATION_TYPE_EP.getExtensions();
        for (ConfigurationType type : types) {
            if (type.getDisplayName().equals("JUnit")) {
                return type;
            }
        }
        return null;
    }
}
