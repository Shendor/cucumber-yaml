package org.shendor.cucumber.yaml.steps

import com.intellij.psi.PsiElement
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.yaml.psi.YAMLSequenceItem

class YamlStepDefinition(private val method: YAMLSequenceItem) : AbstractStepDefinition(method) {
    companion object {
        const val REGEX_START = "^"
        const val REGEX_END = "$"
    }

    override fun getVariableNames() = emptyList<String>()

    override fun getCucumberRegexFromElement(element: PsiElement?): String? {
        val text = getStepDefinitionText() ?: return null
        if (text.startsWith(REGEX_START) || text.endsWith(REGEX_END)) {
            return text
        }
        return CucumberUtil.buildRegexpFromCucumberExpression(text, YamlParameterTypeManager)
    }

    private fun getStepDefinitionText(): String? {
        return CucumberYamlUtil.getStepName(element as YAMLSequenceItem)
    }

    override fun getElement(): PsiElement? {
        return YamlTestCasePsiElement(method as YAMLSequenceItem)
    }

    override fun toString(): String {
        return getStepDefinitionText() ?: ""
    }
}
