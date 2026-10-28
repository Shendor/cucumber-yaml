package org.chronos.intellij.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import org.chronos.intellij.reference.JavaPageReference
import org.chronos.intellij.reference.JavaUiElementReference
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue

class JavaUiUsageSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(parameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = declarationOwner(parameters.elementToSearch) ?: return
        val scope = parameters.effectiveSearchScope
        val fileScope = scope as? GlobalSearchScope ?: GlobalSearchScope.projectScope(target.project)
        val key = if (target is PsiClass) "page" else "element"
        for (file in FileTypeIndex.getFiles(YAMLFileType.YML, fileScope)) {
            ProgressManager.checkCanceled()
            val psiFile = PsiManager.getInstance(target.project).findFile(file) ?: continue
            for (entry in PsiTreeUtil.findChildrenOfType(psiFile, YAMLKeyValue::class.java)) {
                ProgressManager.checkCanceled()
                if (entry.keyText != key || !PsiSearchScopeUtil.isInScope(scope, entry.value ?: entry)) continue
                for (reference in entry.references) {
                    if ((reference is JavaPageReference || reference is JavaUiElementReference) &&
                        reference.isReferenceTo(target) && !consumer.process(reference)
                    ) return
                }
            }
        }
    }

    companion object {
        fun declarationOwner(element: PsiElement): PsiModifierListOwner? {
            val owner = when (element) {
                is PsiClass, is PsiField -> element as PsiModifierListOwner
                else -> {
                    val annotation = element as? PsiAnnotation
                        ?: PsiTreeUtil.getParentOfType(element, PsiAnnotation::class.java) ?: return null
                    val annotationOwner = (annotation.parent as? PsiModifierList)?.parent
                    val expectedName = when (annotationOwner) {
                        is PsiClass -> "PageAccessor"
                        is PsiField -> "Locator"
                        else -> return null
                    }
                    if (annotation.nameReferenceElement?.referenceName != expectedName) return null
                    // Only the declaration annotation and its name attribute represent the UI name.
                    if (element != annotation) {
                        val pair = PsiTreeUtil.getParentOfType(element, PsiNameValuePair::class.java) ?: return null
                        if (pair.name != "name") return null
                    }
                    annotationOwner as PsiModifierListOwner
                }
            }
            val annotationName = when (owner) {
                is PsiClass -> "PageAccessor"
                is PsiField -> "Locator"
                else -> return null
            }
            return owner.takeIf {
                it.modifierList?.annotations?.any { annotation ->
                    annotation.nameReferenceElement?.referenceName == annotationName
                } == true
            }
        }
    }
}
