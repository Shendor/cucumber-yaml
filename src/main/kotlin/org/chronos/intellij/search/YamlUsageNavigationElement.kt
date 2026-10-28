package org.chronos.intellij.search

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/** A usage with its own presentation for the symbol-based Choose Declaration popup. */
class YamlUsageNavigationElement(val usage: PsiElement) : FakePsiElement() {
    override fun getParent() = usage
    override fun getContainingFile() = usage.containingFile
    override fun getName() = usage.text.lineSequence().first().trim()
    override fun getPresentableText() = name
    override fun getLocationString() = containingFile.name
    override fun getIcon(open: Boolean) = containingFile.fileType.icon
    override fun getIcon(flags: Int) = containingFile.fileType.icon
    override fun getText() = usage.text
    override fun getTextRange() = usage.textRange
    override fun getTextOffset() = usage.textOffset
    override fun isValid() = usage.isValid
    override fun canNavigate() = isValid && containingFile.virtualFile != null
    override fun canNavigateToSource() = canNavigate()
    override fun navigationRequest() = descriptor()?.navigationRequest()
    override fun navigate(requestFocus: Boolean) {
        descriptor()?.navigate(requestFocus)
    }

    private fun descriptor(): OpenFileDescriptor? = if (canNavigate()) {
        OpenFileDescriptor(project, containingFile.virtualFile, textOffset)
    } else null

    override fun equals(other: Any?) = other is YamlUsageNavigationElement && usage == other.usage
    override fun hashCode() = usage.hashCode()
}
