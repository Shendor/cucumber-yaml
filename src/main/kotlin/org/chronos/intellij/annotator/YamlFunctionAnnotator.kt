package org.chronos.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar

class YamlFunctionAnnotator : Annotator {
    private val funcStartRegex = Regex("#?[a-zA-Z_]\\w*\\(")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLScalar) return

        val elementText = element.text
        if (!elementText.contains("(")) return

        val startOffset = element.textRange.startOffset
        val funcStarts = funcStartRegex.findAll(elementText)

        val closingBrackets = mutableListOf<Int>()
        val openBracketsIndices = mutableListOf<Int>()

        for (i in elementText.indices) {
            if (elementText[i] == '(') {
                openBracketsIndices.add(i)
            } else if (elementText[i] == ')') {
                if (openBracketsIndices.isNotEmpty()) {
                    val openIdx = openBracketsIndices.removeAt(openBracketsIndices.size - 1)
                    // Check if this '(' was part of a function start
                    if (funcStarts.any { it.range.last == openIdx }) {
                        closingBrackets.add(i)
                    }
                }
            }
        }

        // Highlight function starts
        for (match in funcStarts) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(startOffset + match.range.first, startOffset + match.range.last + 1))
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                .create()
        }

        // Highlight matched closing brackets
        for (bracketIdx in closingBrackets) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(startOffset + bracketIdx, startOffset + bracketIdx + 1))
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                .create()
        }
    }
}
