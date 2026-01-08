package org.chronos.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar

class YamlFunctionAnnotator : Annotator {
    private val regex = Regex("(#?[a-zA-Z_]\\w*\\().*?\\)")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLScalar) return

        val text = element.textValue
        if (!text.contains("(")) return

        val elementText = element.text
        // We need to find matches in elementText to get correct offsets, but textValue might be different (e.g. unquoted)
        // However, the regex should work on the raw text too.
        val matches = regex.findAll(elementText)

        for (match in matches) {
            val funcNameGroup = match.groups[1] ?: continue
            
            // Highlight #func part
            val funcStart = element.textRange.startOffset + funcNameGroup.range.first
            val funcEnd = element.textRange.startOffset + funcNameGroup.range.last + 1
            
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(funcStart, funcEnd))
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                .create()

            // Highlight the last closing bracket
            val lastBracketIndex = match.range.last
            val bracketStart = element.textRange.startOffset + lastBracketIndex
            val bracketEnd = bracketStart + 1
            
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(bracketStart, bracketEnd))
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                .create()
        }
    }
}
