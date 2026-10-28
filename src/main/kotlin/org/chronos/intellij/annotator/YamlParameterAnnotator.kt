package org.chronos.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.chronos.intellij.highlighting.YamlColorSettingsPage
import org.jetbrains.yaml.psi.YAMLScalar

class YamlParameterAnnotator : Annotator {
    private val regex = Regex("<|>")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLScalar) return

        val elementText = element.text
        val matches = regex.findAll(elementText)

        for (match in matches) {
            val start = element.textRange.startOffset + match.range.first
            val end = element.textRange.startOffset + match.range.last + 1

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(start, end))
                .textAttributes(YamlColorSettingsPage.YAML_PARAMETER)
                .create()
        }
    }
}
