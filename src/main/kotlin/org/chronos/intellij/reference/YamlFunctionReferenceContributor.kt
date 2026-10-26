package org.chronos.intellij.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLScalar

/** References use raw PSI offsets so quotes, escapes and multiline scalars retain correct ranges. */
class YamlFunctionReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(YAMLScalar::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
                    FUNCTION_CALL.findAll(element.text).map { match ->
                        val name = match.groups[1]!!
                        YamlFunctionReference(
                            element,
                            TextRange(match.range.first, name.range.last + 1),
                            name.value
                        )
                    }.toList().toTypedArray()
            }
        )
    }

    companion object {
        private val FUNCTION_CALL = Regex("#([a-zA-Z_][a-zA-Z_0-9]*)\\s*\\(")
    }
}
