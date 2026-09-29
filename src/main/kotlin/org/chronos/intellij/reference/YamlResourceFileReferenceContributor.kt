package org.chronos.intellij.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLScalar

class YamlResourceFileReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLScalar::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val scalar = element as? YAMLScalar ?: return PsiReference.EMPTY_ARRAY
                    val text = scalar.textValue
                    if (text.isEmpty()) return PsiReference.EMPTY_ARRAY

                    val references = mutableListOf<PsiReference>()
                    val regex = Regex("ftl\\(([^)]+)\\)")
                    val matches = regex.findAll(text)

                    val elementText = scalar.text
                    val contentOffset = elementText.indexOf(text)
                    if (contentOffset == -1) return PsiReference.EMPTY_ARRAY

                    for (match in matches) {
                        val group = match.groups[1] ?: continue
                        val range = group.range

                        val start = contentOffset + range.first
                        val end = contentOffset + range.last + 1

                        references.add(YamlResourceFileReference(element, TextRange(start, end)))
                    }

                    return references.toTypedArray()
                }
            }
        )
    }
}
