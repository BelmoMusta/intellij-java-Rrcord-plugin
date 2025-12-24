package musta.belmo.plugins.ast;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiRecordHeader;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PsiJavaRecordTransformer implements Transformer {

    public PsiJavaRecordTransformer() {
    }

    public void transformPsi(PsiElement psiElement) {
        List<PsiClass> classes = new ArrayList<>();
        if (psiElement instanceof PsiClass psiClass) {
            classes.add(psiClass);
        } else if (psiElement instanceof PsiJavaFile psiJavaFile) {
            classes.addAll(PsiUtils.getAllClassesInFile(psiJavaFile));
        }

        for (PsiClass psiClass : classes) {
            List<PsiField> fields = handleFields(psiClass);
            PsiClass aRecord = createRecord(psiClass, fields);
            psiClass.replace(aRecord);
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
        PsiClass aRecord = instance.createRecord(psiClass.getName());
        String[] fieldNames = new String[fields.size()];
        PsiType[] fieldTypes = new PsiType[fields.size()];

        int i = 0;
        for (PsiField field : fields) {
            fieldNames[i] = field.getName();
            fieldTypes[i] = field.getType();
            i++;
        }

        PsiElement parameter = instance.createParameterList(fieldNames, fieldTypes);
        String listParams = parameter.getText();
        listParams = listParams.substring(1, listParams.length() - 1);
        PsiRecordHeader header = instance.createRecordHeaderFromText(listParams, aRecord);
        aRecord.getRecordHeader().replace(header);
        RecordUtil.copy(psiClass, aRecord, fields);
        return aRecord;
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
