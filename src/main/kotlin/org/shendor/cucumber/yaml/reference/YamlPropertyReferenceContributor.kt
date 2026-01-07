package org.shendor.cucumber.yaml.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLScalar

class YamlPropertyReferenceContributor : PsiReferenceContributor() {
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
                    val regex = Regex("\\\$\\{([^}]+)}")
                    val matches = regex.findAll(text)
                    
                    // We need the offset of the content within the element's text (e.g., skip quotes)
                    val elementText = scalar.text
                    // YAML scalar text might have quotes, textValue doesn't.
                    // We try to find the match in the element text to be more precise if possible, 
                    // but since ${} can contain special chars, we just use the offset of textValue.
                    val contentOffset = elementText.indexOf(text)
                    if (contentOffset == -1) return PsiReference.EMPTY_ARRAY
                    
                    for (match in matches) {
                        val group = match.groups[1] ?: continue
                        val range = group.range
                        
                        // range is relative to 'text' (textValue)
                        val start = contentOffset + range.first
                        val end = contentOffset + range.last + 1
                        
                        references.add(YamlPropertyReference(element, TextRange(start, end)))
                    }
                    
                    return references.toTypedArray()
                }
            }
        )
    }
}
