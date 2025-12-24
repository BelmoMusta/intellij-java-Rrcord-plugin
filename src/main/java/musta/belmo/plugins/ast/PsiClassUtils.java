package musta.belmo.plugins.ast;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class PsiClassUtils {
    public static void deleteMethods(List<PsiClass> classes) {
        List<String> methodPrefixesToDelete = Arrays.asList("set", "get", "is");
        List<Predicate<PsiMethod>> methodPredicates = new ArrayList<>();
        for (String prefix : methodPrefixesToDelete) {
            Predicate<PsiMethod> predicate = psiMethod -> psiMethod.getName().startsWith(prefix);
            methodPredicates.add(predicate);
        }
        for (Predicate<PsiMethod> methodPredicate : methodPredicates) {
            PsiMethodUtils.deleteMethods(classes, methodPredicate);
        }
    }
    public static void deleteConstructors(List<PsiClass> classes) {
        for (PsiClass aClass : classes) {
            PsiMethod[] constructors = aClass.getConstructors();
            for (PsiMethod constructor : constructors) {
                if (!constructor.hasParameters()) {
                    PsiMethodUtils.deleteConstructor(constructor);
                }
            }
            int fieldsCount = aClass.getFields().length;
            for (PsiMethod constructor : constructors) {
                int paramsCount = constructor.getParameterList().getParameters().length;
                if (fieldsCount == paramsCount) {
                    PsiMethodUtils.deleteConstructor(constructor);
                }
            }
        }
    }
}
