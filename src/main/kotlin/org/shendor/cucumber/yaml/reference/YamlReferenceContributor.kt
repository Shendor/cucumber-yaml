package org.shendor.cucumber.yaml.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.shendor.cucumber.yaml.search.YamlStepDeclaration
import org.shendor.cucumber.yaml.steps.YamlStepDefinition

class YamlReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val value = getStepDefName(element)
                    if (value != null) {
                        val property = TextRange(1, value.length + 1)
                        return arrayOf(YamlReference(element, property))
                    }
                    return PsiReference.EMPTY_ARRAY
                }
            })
    }

    private fun getStepDefName(element: PsiElement): String? {
        val literalExpression = element as PsiLiteralExpression
        return PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression::class.java)?.let {
            if (it.text.startsWith("runTestCase") && literalExpression.value is String)
                literalExpression.value as String? else null
        }
    }

}