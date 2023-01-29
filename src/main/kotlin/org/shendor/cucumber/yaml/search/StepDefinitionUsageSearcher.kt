package org.shendor.cucumber.yaml.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import net.lagerwey.plugins.cucumber.kotlin.inReadAction
import org.shendor.cucumber.yaml.steps.YamlTestCasePsiElement
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.shendor.cucumber.yaml.steps.YamlStepDefinition

class StepDefinitionUsageSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val elementToSearch = queryParameters.elementToSearch
        var element: PsiElement = queryParameters.elementToSearch
        var stepName = ""
        if (elementToSearch is PomTargetPsiElement) {
            val declaration = elementToSearch.target
            if (declaration is YamlStepDeclaration) {
                element = declaration.element
                stepName = declaration.stepName
            }
        } else if (element is YAMLSequenceItem) {
            stepName = CucumberYamlUtil.getStepName(element) ?: "none"
        }

        if (element is YAMLSequenceItem) {
//        val name = YamlStepDefinition.Companion.PARAM_REPLACEMENT_PATTERN.matcher(stepName).replaceAll("(.+)")
            inReadAction {
                CucumberUtil.findGherkinReferencesToElement(
                    element as YAMLSequenceItem,
//                YamlTestCasePsiElement(declaration.element as YAMLSequenceItem),
                    stepName,
//                "$name[\\.:>]",
                    consumer,
                    queryParameters.effectiveSearchScope
                )
            }
        }
    }
}
