package org.shendor.cucumber.yaml.search

import com.intellij.ide.util.EditSourceUtil
import com.intellij.pom.PomNamedTarget
import com.intellij.psi.PsiElement

data class YamlStepDeclaration(val element: PsiElement, val stepName: String) : PomNamedTarget {
    override fun canNavigate() = EditSourceUtil.canNavigate(element)

    override fun canNavigateToSource() = canNavigate()

    override fun getName() = stepName

    override fun isValid() = element.isValid

    override fun toString(): String {
        return stepName
    }

    override fun navigate(requestFocus: Boolean) {
        EditSourceUtil.getDescriptor(element)?.navigate(requestFocus)
    }
}
