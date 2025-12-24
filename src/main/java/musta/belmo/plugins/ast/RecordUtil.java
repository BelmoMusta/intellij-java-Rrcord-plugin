package musta.belmo.plugins.ast;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecordUtil {
    public static void copy(PsiClass source, PsiClass destination, List<PsiField> fields) {
        List<PsiElement> innerChildren = new ArrayList<>();
        int y = 0;
        for (int i = 0; i < source.getChildren().length; i++) {
            PsiElement child = source.getChildren()[i];
            if (child instanceof PsiJavaToken) {
                if (child.getText().equals("{")) {
                    y = i + 1;
                }
            }
        }

        for (int i = y; i < source.getChildren().length - 1; i++) {
            innerChildren.add(source.getChildren()[i]);
        }

        for (@NotNull PsiElement child : innerChildren) {
            if (child instanceof PsiMethod method) {
                if (isACopiableMethod(method, fields)) {
                    destination.add(child);
                }
            } else {
                destination.add(child);
            }
        }
    }
    private static boolean isACopiableMethod(@NotNull PsiMethod child, List<PsiField> fields) {
        return !PsiMethodUtils.isAssociatedWithAField(child, fields);
    }
}
