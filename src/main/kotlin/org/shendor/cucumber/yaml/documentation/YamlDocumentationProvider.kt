package org.shendor.cucumber.yaml.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiElement
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import org.jetbrains.yaml.psi.YAMLKeyValue

class YamlDocumentationProvider : AbstractDocumentationProvider() {

    private val nameToFile = mutableMapOf<String, String>()

    init {
        nameToFile["expected response"] = "validate"
        nameToFile["expected message"] = "validate"
        nameToFile["assert"] = "validate"
        nameToFile["verify response"] = "validate"
        nameToFile["verify"] = "validate"
        nameToFile["validates"] = "validate"

        nameToFile["request"] = "payload"
        nameToFile["message"] = "payload"
        nameToFile["body"] = "payload"
    }

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
            val key = nameToFile[element.keyText] ?: element.keyText
            val text = YamlDocumentationProvider::class.java.getResource("/doc/$key.html")?.readText()
            return text?.let { renderFullDoc(key, text) }
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
     * Creates the formatted documentation using [DocumentationMarkup]. See the Java doc of
     * [com.intellij.lang.documentation.DocumentationProvider.generateDoc] for more
     * information about building the layout.
     */
    private fun renderFullDoc(key: String, docComment: String): String {
        val sb = StringBuilder()
        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("Chronos keyword: $key")
        sb.append(DocumentationMarkup.DEFINITION_END)
        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append(docComment)
        sb.append(DocumentationMarkup.CONTENT_END)

        return sb.toString()
    }
}