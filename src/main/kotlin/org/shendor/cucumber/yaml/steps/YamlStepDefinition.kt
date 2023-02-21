package org.shendor.cucumber.yaml.steps

import com.intellij.psi.PsiElement
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.yaml.psi.YAMLSequenceItem
import java.util.regex.Pattern

class YamlStepDefinition(private val method: YAMLSequenceItem) : AbstractStepDefinition(method) {
    companion object {
        const val REGEX_START = "^"
        const val REGEX_END = "$"
        val PARAM_REPLACEMENT_PATTERN: Pattern = Pattern.compile("<.+>")
    }

    override fun getVariableNames() = emptyList<String>()

    override fun getCucumberRegexFromElement(element: PsiElement?): String? {
        return CucumberYamlUtil.getStepNameAsRegex(element as YAMLSequenceItem)
    }

    private fun getStepDefinitionText(): String? {
        return CucumberYamlUtil.getStepName(element as YAMLSequenceItem)
    }

    override fun getElement(): PsiElement? {
        return method
//        return YamlTestCasePsiElement(method)
    }

    override fun toString(): String {
        return getStepDefinitionText() ?: ""
    }
}
