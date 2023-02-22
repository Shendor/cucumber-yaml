package org.shendor.cucumber.yaml.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import org.jetbrains.yaml.psi.YAMLKeyValue

class YamlDocumentationProvider : AbstractDocumentationProvider() {
    /**
     * For the Simple Language, we don't have online documentation. However, if your language provides
     * references pages online, URLs for the element can be returned here.
     */
    override fun getUrlFor(element: PsiElement, originalElement: PsiElement): List<String>? {
        return null
    }

    /**
     * Extracts the key, value, file and documentation comment of a Simple key/value entry and returns
     * a formatted representation of the information.
     */
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element is YAMLKeyValue) {
            val key = element.keyText
            val file = SymbolPresentationUtil.getFilePathPresentation(element.getContainingFile())
            val docComment = "keyword description goes here"
            return renderFullDoc(key, file, docComment)
        }
        return null
    }

    /**
     * Provides the information in which file the Simple language key/value is defined.
     */
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String? {
        if (element is YAMLKeyValue) {
            val key = element.keyText
            val file = SymbolPresentationUtil.getFilePathPresentation(element.getContainingFile());
            return "\"$key\" in $file"
        }
        return null
    }

    /**
     * Provides documentation when a Simple Language element is hovered with the mouse.
     */
    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }

    /**
     * Creates a key/value row for the rendered documentation.
     */
    private fun addKeyValueSection(key: String, value: String, sb: StringBuilder) {
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
        sb.append(key)
        sb.append(DocumentationMarkup.SECTION_SEPARATOR)
        sb.append("<p>")
        sb.append(value)
        sb.append(DocumentationMarkup.SECTION_END)
    }

    /**
     * Creates the formatted documentation using [DocumentationMarkup]. See the Java doc of
     * [com.intellij.lang.documentation.DocumentationProvider.generateDoc] for more
     * information about building the layout.
     */
    private fun renderFullDoc(key: String, file: String, docComment: String): String {
        val sb = StringBuilder()
        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("Chronos keyword")
        sb.append(DocumentationMarkup.DEFINITION_END)
        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append(key)
        sb.append(DocumentationMarkup.CONTENT_END)
        sb.append(DocumentationMarkup.SECTIONS_START)
        addKeyValueSection("", docComment, sb)
        sb.append(DocumentationMarkup.SECTIONS_END)
        return sb.toString()
    }
}