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
                    if (element is YAMLKeyValue && element.value != null) {
                        val value = element.valueText
                        if (element.keyText == "parent") {
                            return arrayOf(YamlReference(element.value!!, TextRange(0, value.length)))
                        } else if (element.keyText == "element") {
                            return arrayOf(JavaUiElementReference(element.value!!, TextRange(0, value.length)))
                        }
                    }

                    return PsiReference.EMPTY_ARRAY
                }
            })
    }

}