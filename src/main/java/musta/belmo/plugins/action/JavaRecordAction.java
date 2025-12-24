package musta.belmo.plugins.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import musta.belmo.plugins.ast.PsiJavaRecordTransformer;
import musta.belmo.plugins.ast.Transformer;
import org.jetbrains.annotations.NotNull;

public class JavaRecordAction extends AbstractAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    @Override
    protected Transformer getTransformer() {
        return new PsiJavaRecordTransformer();
    }
}