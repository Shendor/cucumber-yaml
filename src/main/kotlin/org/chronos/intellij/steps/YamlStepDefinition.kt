package org.chronos.intellij.steps

import com.intellij.psi.PsiElement
import org.chronos.intellij.CucumberYamlUtil
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.yaml.psi.YAMLKeyValue

class YamlStepDefinition(private val method: YAMLKeyValue) : AbstractStepDefinition(method) {
    companion object {
        const val REGEX_START = "^"
        const val REGEX_END = "$"
    }

    override fun getVariableNames() = emptyList<String>()

    override fun getCucumberRegexFromElement(element: PsiElement?): String {
        return CucumberYamlUtil.getStepNameAsRegex(element as YAMLKeyValue)
    }

    private fun getStepDefinitionText(): String? {
        return CucumberYamlUtil.getStepName(element as YAMLKeyValue)
    }

    override fun getElement(): PsiElement {
        return YamlTestCasePsiElement(method)
    }

    override fun toString(): String {
        return getStepDefinitionText() ?: ""
    }
}
