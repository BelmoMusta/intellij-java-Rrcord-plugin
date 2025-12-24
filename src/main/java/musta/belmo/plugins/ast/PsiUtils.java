package musta.belmo.plugins.ast;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import musta.belmo.plugins.action.DfsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PsiUtils {
    @NotNull
    static List<PsiClass> getAllClassesInFile(PsiJavaFile psiJavaFile) {
        List<PsiClass> psiClasses = new ArrayList<>();
        for (PsiClass aClass : psiJavaFile.getClasses()) {
            DfsBuilder<PsiClass> dfsBuilder = new DfsBuilder<>();
            dfsBuilder.root(aClass)
                    .includeRoot(true)
                    .leafPredicate(psiClass -> psiClass.getInnerClasses().length == 0)
                    .nodePredicate(psiClass -> psiClass.getInnerClasses().length != 0)
                    .directChildrenGetter(aCls -> Arrays.asList(aCls.getInnerClasses()))
                    .retainFilter(cls -> !cls.isInterface() && !cls.isEnum());
            return dfsBuilder.toList();

        }
        return psiClasses;
    }
    public static List<PsiElement> getAllJavaFiles(PsiElement dir) {
        DfsBuilder<PsiElement> dfsBuilder = new DfsBuilder<>();
        dfsBuilder.root(dir);
        dfsBuilder.nodePredicate(element -> element instanceof PsiDirectory);
        dfsBuilder.leafPredicate(element -> element instanceof PsiJavaFile);
        dfsBuilder.directChildrenGetter(psiElement -> Arrays.asList(psiElement.getChildren()));
        return dfsBuilder.toList();
    }
    /**
     * uncapitalize the inout String
     *
     * @param input @link String}
     * @return String
     */
    public static String capitalize(String input) {
        String output = input;
        if (input != null && !input.isEmpty()) {
            output = Character.toUpperCase(input.charAt(0)) + input.substring(1);
        }
        return output;
    }
}
