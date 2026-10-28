package org.chronos.intellij.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLMapping
import org.chronos.intellij.CucumberYamlUtil

class ParentYamlReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLKeyValue::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val entry = element as? YAMLKeyValue ?: return PsiReference.EMPTY_ARRAY
                    if (entry.keyText !in setOf("parent", "element", "page", "test", "data-test", "name")) return PsiReference.EMPTY_ARRAY
                    if (entry.keyText == "page" &&
                        (entry.parent as? YAMLMapping)?.keyValues?.any(CucumberYamlUtil::isStepDefinition) != true
                    ) return PsiReference.EMPTY_ARRAY
                    val value = entry.value as? YAMLScalar ?: return PsiReference.EMPTY_ARRAY
                    if (value.textValue.isEmpty()) return PsiReference.EMPTY_ARRAY

                    // References must belong to the element passed to this provider.
                    // Translate the scalar's content range (excluding quotes) into that element.
                    val range = ElementManipulators.getValueTextRange(value)
                        .shiftRight(value.textRange.startOffset - entry.textRange.startOffset)
                    return when (entry.keyText) {
                        "parent" -> arrayOf(YamlReference(entry, range))
                        "page" -> arrayOf(JavaPageReference(entry, range, value.textValue))
                        "test", "data-test", "name" -> arrayOf(YamlDataTestReference(entry, range))
                        else -> arrayOf(JavaUiElementReference(entry, range))
                    }
                }
            })
    }

}
