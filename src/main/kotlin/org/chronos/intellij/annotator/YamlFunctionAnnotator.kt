package org.chronos.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.chronos.intellij.highlighting.YamlColorSettingsPage
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class YamlFunctionAnnotator : Annotator {
    private val funcStartRegex = Regex("#?[a-zA-Z_]\\w*\\(")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLScalar && element !is YAMLKeyValue) return

        val elementText = if (element is YAMLKeyValue) element.key?.text ?: "" else element.text
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
                .textAttributes(YamlColorSettingsPage.YAML_FUNCTION)
                .create()
        }

        // Highlight matched closing brackets
        for (bracketIdx in closingBrackets) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(startOffset + bracketIdx, startOffset + bracketIdx + 1))
                .textAttributes(YamlColorSettingsPage.YAML_FUNCTION)
                .create()
        }
    }
}
