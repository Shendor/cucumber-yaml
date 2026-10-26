package org.chronos.intellij.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.chronos.intellij.CucumberYamlUtil
import org.jetbrains.yaml.psi.YAMLKeyValue

class JavaUiElementReference(element: PsiElement, textRange: TextRange) :
    PsiPolyVariantReferenceBase<PsiElement>(element, textRange) {
    private val uiElementName = (element as? YAMLKeyValue)?.valueText
        ?: textRange.substring(element.text)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        findElements().filter { getLocationName(it) == uiElementName }
            .map { PsiElementResolveResult(it) }.toTypedArray()

    override fun isReferenceTo(target: PsiElement): Boolean = multiResolve(false).any {
        val annotation = it.element as? PsiAnnotation ?: return@any false
        val field = (annotation.parent as? PsiModifierList)?.parent
        element.manager.areElementsEquivalent(annotation, target) ||
            (field != null && element.manager.areElementsEquivalent(field, target))
    }

    override fun getVariants(): Array<Any> = findElements().mapNotNull { annotation ->
        getLocationName(annotation)?.let { name ->
            LookupElementBuilder.create(annotation, name).withTypeText(annotation.containingFile.name)
        }
    }.toTypedArray()

    private fun findElements(): List<PsiAnnotation> =
        ModuleUtilCore.findModuleForPsiElement(element)?.let(CucumberYamlUtil::findJavaUiElements) ?: emptyList()

    private fun getLocationName(annotation: PsiAnnotation): String? {
        val value = annotation.findAttributeValue("name") ?: return null
        return JavaPsiFacade.getInstance(element.project).constantEvaluationHelper
            .computeConstantExpression(value) as? String
    }
}
