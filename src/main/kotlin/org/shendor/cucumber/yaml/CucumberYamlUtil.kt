package org.shendor.cucumber.yaml

import org.jetbrains.yaml.psi.YAMLSequenceItem

object CucumberYamlUtil {
    const val CUCUMBER_PACKAGE = "io.cucumber.java8"

    fun isStepDefinition(candidate: YAMLSequenceItem): Boolean {
        return candidate.keysValues.firstOrNull { it.keyText == "test" }?.let { true }?:false
    }

    fun getStepName(stepDefinition: YAMLSequenceItem): String? {
        val keywordExpression = stepDefinition.keysValues.firstOrNull { it.keyText == "test" }
        return keywordExpression?.value?.text
    }
}
