package org.chronos.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar
import java.awt.Font
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.markup.EffectType

class YamlPropertyKeyAnnotator : Annotator {
    private val regex = Regex("\\\$\\{([^}]+)}")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLScalar) return

        val elementText = element.text
        val matches = regex.findAll(elementText)

        for (match in matches) {
            val group = match.groups[1] ?: continue
            val start = element.textRange.startOffset + group.range.first
            val end = element.textRange.startOffset + group.range.last + 1

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(start, end))
                .enforcedTextAttributes(TextAttributes(null, null, null, EffectType.LINE_UNDERSCORE, Font.ITALIC))
                .create()
        }
    }
}
