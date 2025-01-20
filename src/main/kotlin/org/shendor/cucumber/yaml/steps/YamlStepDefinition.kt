package org.shendor.cucumber.yaml.steps

import com.intellij.psi.PsiElement
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequenceItem
import java.util.regex.Pattern

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
