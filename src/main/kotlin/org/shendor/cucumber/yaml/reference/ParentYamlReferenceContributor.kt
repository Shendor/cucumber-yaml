package org.shendor.cucumber.yaml.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue

class ParentYamlReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLKeyValue::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val value = getStepDefName(element)
                    if (value != null) {
                        val property = TextRange(0, value.length)
                        return arrayOf(YamlReference((element as YAMLKeyValue).value!!, property))
                    }
                    return PsiReference.EMPTY_ARRAY
                }
            })
    }

    private fun getStepDefName(element: PsiElement): String? {
        return if (element is YAMLKeyValue) {
            if (element.keyText == "parent") element.valueText else null
        } else null
    }

}