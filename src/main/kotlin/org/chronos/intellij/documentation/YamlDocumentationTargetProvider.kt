package org.chronos.intellij.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class YamlDocumentationTargetProvider : DocumentationTargetProvider, PsiDocumentationTargetProvider {

    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        val element = file.findElementAt(offset) ?: return emptyList()
        val word = getWordAt(element) ?: return emptyList()
        val docContent = loadDocFor(word) ?: return emptyList()

        return listOf(YamlDocumentationTarget(element, word, docContent))
    }

    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val targetElement = originalElement ?: element
        val word = getWordAt(targetElement) ?: return null
        val docContent = loadDocFor(word) ?: return null

        return YamlDocumentationTarget(targetElement, word, docContent)
    }

    private fun getWordAt(element: PsiElement): String? {
        val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java, false)
        if (keyValue != null && (keyValue.key == element || keyValue.key == element.parent)) {
            return keyValue.keyText
        }

        val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java, false)
        if (scalar != null) {
            val text = scalar.text
            val offset = element.textRange.startOffset - scalar.textRange.startOffset
            if (offset < 0 || offset >= text.length) return null
            return extractWordAt(text, offset)
        }

        val text = element.text
        if (text.isNullOrBlank()) return null
        return extractWordAt(text, 0)
    }

    private fun extractWordAt(text: String, offset: Int): String {
        var start = offset
        while (start > 0 && isWordPart(text[start - 1])) {
            start--
        }
        var end = offset
        while (end < text.length && isWordPart(text[end])) {
            end++
        }
        return text.substring(start, end)
    }

    private fun isWordPart(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_' || c == '-' || c == '#' || c == '.' || c == ' '
    }

    private fun loadDocFor(word: String): String? {
        var doc = this::class.java.getResource("/doc/$word.html")?.readText()
        if (doc != null) return doc

        val normalized = word.trim().removePrefix("#").removeSuffix(":")
        doc = this::class.java.getResource("/doc/$normalized.html")?.readText()
        return doc
    }
}

class YamlDocumentationTarget(
    private val element: PsiElement,
    private val word: String,
    private val docContent: String
) : DocumentationTarget {

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder("Chronos: $word").presentation()
    }

    override fun computeDocumentation(): com.intellij.platform.backend.documentation.DocumentationResult? {
        val sb = StringBuilder()
        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("Chronos: $word")
        sb.append(DocumentationMarkup.DEFINITION_END)
        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append(docContent)
        sb.append(DocumentationMarkup.CONTENT_END)
        return com.intellij.platform.backend.documentation.DocumentationResult.documentation(sb.toString())
    }

    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPointer = element.createSmartPointer()
        val word = this.word
        val docContent = this.docContent
        return Pointer {
            val element = elementPointer.element ?: return@Pointer null
            YamlDocumentationTarget(element, word, docContent)
        }
    }
}
