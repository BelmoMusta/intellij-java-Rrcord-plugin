package musta.belmo.plugins.ast;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiRecordHeader;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.LightKeyword;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PsiJavaRecordTransformer implements Transformer {

    public void transformPsi(PsiElement psiElement) {
        List<PsiClass> classes = new ArrayList<>();
        if (psiElement instanceof PsiClass psiClass) {
            classes.add(psiClass);
        } else if (psiElement instanceof PsiJavaFile psiJavaFile) {
            classes.addAll(PsiUtils.getAllClassesInFile(psiJavaFile));
        }

        for (PsiClass psiClass : classes) {
            Project project = psiClass.getProject();
            PsiFile containingFile = psiClass.getContainingFile();
            PsiClass psiClassCopy = (PsiClass) psiClass.copy();
            handleMethods(psiClassCopy);
            handleConstructors(psiClassCopy);
            List<PsiField> fields = handleFields(psiClassCopy);
            PsiClass aRecord = createRecord(psiClassCopy, fields);
            psiClass.replace(aRecord);
            //fileDocument.setText(aRecord.getText());
            Document document = PsiDocumentManager.getInstance(project).getCachedDocument(containingFile);
            commit(document, project);
        }
    }

    private static void handleConstructors(PsiClass psiClass) {
        for (PsiMethod constructor : psiClass.getConstructors()) {
            constructor.delete();
        }
    }
    private static void handleMethods(PsiClass psiClass) {
        for (PsiField field : psiClass.getFields()) {
            for (PsiMethod method : psiClass.getMethods()) {
                if (method.hasModifierProperty("static")){
                    continue;
                }
                if (method.getName().equalsIgnoreCase("get" + field.getName()) && !method.hasParameters()) {
                    method.delete();
                }
                if (method.getName().equalsIgnoreCase("is" + field.getName()) && !method.hasParameters() && field.getType().equalsToText("boolean")) {
                    method.delete();
                }
                if (method.getName().equalsIgnoreCase("set" + field.getName()) && method.getParameterList().getParameters().length == 1 && method.getParameterList().getParameters()[0].getType().equals(field.getType())) {
                    method.delete();
                }
            }
        }

    }
    private static @NotNull List<PsiField> handleFields(PsiClass psiClass) {
        List<PsiField> fields = new ArrayList<>();
        for (PsiField field : psiClass.getFields()) {
            if (field.getModifierList() != null && field.getModifierList().hasModifierProperty("static")) {
                continue;
            }
            fields.add((PsiField) field.copy());
            field.delete();
        }
        return fields;
    }
    private static PsiClass createRecord(PsiClass psiClass, List<PsiField> fields) {
        PsiElementFactory instance = PsiElementFactory.getInstance(psiClass.getProject());
        PsiClass copy = (PsiClass) psiClass.copy();
        for (@NotNull PsiElement child : copy.getChildren()) {
            if (isaClassKeyword(child)) {
                PsiManager manager = PsiManager.getInstance(psiClass.getProject());
                PsiKeyword recordKeyWord = new LightKeyword(manager, "record");
                child.replace(recordKeyWord);
            }
        }
        for (PsiElement child : copy.getChildren()) {
            if (child instanceof PsiIdentifier identifier && identifier.getText().equals(psiClass.getName())) {
                PsiRecordHeader header = createHeader(psiClass, fields, instance);
                copy.addAfter(header, identifier);
            }
        }
        return copy;
    }
    private static boolean isaClassKeyword(@NotNull PsiElement child) {
        return child instanceof PsiKeyword && child.getText().equals("class");
    }
    private static void commit(Document document, Project project) {
        if (document != null) {
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }
    }
    private static PsiRecordHeader createHeader(PsiClass psiClass,
                                                List<PsiField> fields,
                                                PsiElementFactory instance) {
        int i = 0;
        String[] fieldNames = new String[fields.size()];
        PsiType[] fieldTypes = new PsiType[fields.size()];

        for (PsiField field : fields) {
            fieldNames[i] = field.getName();
            fieldTypes[i] = field.getType();
            i++;
        }
        PsiElement parameter = instance.createParameterList(fieldNames, fieldTypes);
        String listParams = parameter.getText();
        listParams = listParams.substring(1, listParams.length() - 1);
        return instance.createRecordHeaderFromText(listParams, psiClass.copy());
    }

    @Override
    public String getActionName() {
        return "Java Record";
    }
    @Override
    public boolean isApplied() {
        return true;
    }
}
