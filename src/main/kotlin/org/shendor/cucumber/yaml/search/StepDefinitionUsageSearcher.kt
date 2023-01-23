package org.shendor.cucumber.yaml.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import net.lagerwey.plugins.cucumber.kotlin.inReadAction
import org.shendor.cucumber.yaml.steps.YamlTestCasePsiElement
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.yaml.psi.YAMLSequenceItem

class StepDefinitionUsageSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val elementToSearch = queryParameters.elementToSearch
        if (elementToSearch !is PomTargetPsiElement) return

        val declaration = elementToSearch.target
        if (declaration !is YamlStepDeclaration) return

        inReadAction {
            CucumberUtil.findGherkinReferencesToElement(
                YamlTestCasePsiElement(declaration.element as YAMLSequenceItem),
                declaration.stepName,
                consumer,
                queryParameters.effectiveSearchScope
            )
        }
    }
}
