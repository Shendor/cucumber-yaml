package org.shendor.cucumber.yaml.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import net.lagerwey.plugins.cucumber.kotlin.inReadAction
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.shendor.cucumber.yaml.CucumberYamlUtil

class StepDefinitionUsageSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val elementToSearch = queryParameters.elementToSearch
        var element: PsiElement = queryParameters.elementToSearch
        if (elementToSearch is PomTargetPsiElement) {
            val declaration = elementToSearch.target
            if (declaration is YamlStepDeclaration) {
                element = declaration.element
            }
        }

        if (element is YAMLSequenceItem) {
            inReadAction {
                CucumberUtil.findGherkinReferencesToElement(
                    element,
                    CucumberYamlUtil.getStepNameAsRegex(element),
                    consumer,
                    queryParameters.effectiveSearchScope
                )
            }
        }
    }
}
