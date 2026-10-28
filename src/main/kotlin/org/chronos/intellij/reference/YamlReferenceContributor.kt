package org.chronos.intellij.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

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
                        val property = ElementManipulators.getValueTextRange(element)
                        return arrayOf(YamlReference(element, property))
                    }
                    return PsiReference.EMPTY_ARRAY
                }
            })
    }

    private fun getStepDefName(element: PsiElement): String? {
        val literalExpression = element as PsiLiteralExpression
        val arguments = literalExpression.parent as? PsiExpressionList ?: return null
        val call = arguments.parent as? PsiMethodCallExpression ?: return null
        return if (call.methodExpression.referenceName == "runTestCase" &&
            arguments.expressions.firstOrNull() == literalExpression
        ) literalExpression.value as? String else null
    }

}