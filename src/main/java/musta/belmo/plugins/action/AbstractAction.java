package musta.belmo.plugins.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import musta.belmo.plugins.ast.PsiUtils;
import musta.belmo.plugins.ast.Transformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAction extends AnAction {

    private Transformer transformer;
    protected Project project;


    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(AnActionEvent e) {
        project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return;
        }
        List<PsiElement> selectedFiles = new ArrayList<>();
        PsiElement selectedClass = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (selectedClass instanceof PsiClass) {
            selectedFiles.add(selectedClass);
        } else {
            PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
            Navigatable navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
            if (psiFile instanceof PsiJavaFile psiJavaFile) {
                selectedFiles.add(psiJavaFile);
            } else if (navigatable instanceof PsiJavaDirectoryImpl directory) {
                selectedFiles.addAll(PsiUtils.getAllJavaFiles(directory));
            }
        }

        if (!selectedFiles.isEmpty()) {
            applyAction(event, selectedFiles);
        }
    }
    private void applyAction(@NotNull AnActionEvent event, List<PsiElement> psiElements) {
        transformer = getTransformer();

        if (transformer != null && transformer.isApplied()) {
            try {
                WriteCommandAction.writeCommandAction(getEventProject(event))
                        .withName(transformer.getActionName())
                        .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
                        .run(() -> {
                            for (PsiElement psiElement : psiElements) {
                                transformer.transformPsi(psiElement);
                            }
                        });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Nullable
    protected abstract Transformer getTransformer();
}
