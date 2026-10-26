package org.chronos.intellij.search

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import org.chronos.intellij.CucumberYamlUtil
import org.chronos.intellij.reference.YamlReference
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue

/** Direct lookup shared by navigation and Find Usages; never dispatches ReferencesSearch extensions. */
object YamlStepUsages {
    fun process(element: YAMLKeyValue, scope: SearchScope, consumer: Processor<in PsiReference>) {
        if (!CucumberUtil.findGherkinReferencesToElement(
                element, CucumberYamlUtil.getStepNameAsRegex(element), consumer, scope)
        ) return
        val project = element.project
        val fileScope = scope as? GlobalSearchScope ?: GlobalSearchScope.allScope(project)
        val manager = PsiManager.getInstance(project)
        for (file in FileTypeIndex.getFiles(JavaFileType.INSTANCE, fileScope)) {
            ProgressManager.checkCanceled()
            val psiFile = manager.findFile(file) ?: continue
            for (literal in PsiTreeUtil.findChildrenOfType(psiFile, PsiLiteralExpression::class.java)) {
                ProgressManager.checkCanceled()
                if (!PsiSearchScopeUtil.isInScope(scope, literal)) continue
                for (reference in literal.references.filterIsInstance<YamlReference>()) {
                    if (reference.isReferenceTo(element) && !consumer.process(reference)) return
                }
            }
        }
        for (file in FileTypeIndex.getFiles(YAMLFileType.YML, fileScope)) {
            ProgressManager.checkCanceled()
            val psiFile = manager.findFile(file) ?: continue
            for (entry in PsiTreeUtil.findChildrenOfType(psiFile, YAMLKeyValue::class.java)) {
                ProgressManager.checkCanceled()
                if (entry.keyText != "parent" || !PsiSearchScopeUtil.isInScope(scope, entry)) continue
                for (reference in entry.references.filterIsInstance<YamlReference>()) {
                    if (reference.isReferenceTo(element) && !consumer.process(reference)) return
                }
            }
        }
    }
}
