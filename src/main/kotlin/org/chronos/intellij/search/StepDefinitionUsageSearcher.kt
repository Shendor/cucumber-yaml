package org.chronos.intellij.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import org.chronos.intellij.CucumberYamlUtil
import org.jetbrains.yaml.psi.YAMLKeyValue

class StepDefinitionUsageSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val target = queryParameters.elementToSearch
        val element = if (target is PomTargetPsiElement) {
            (target.target as? YamlStepDeclaration)?.element
        } else target
        if (element is YAMLKeyValue && CucumberYamlUtil.isStepDefinition(element)) {
            ChronosStepUsages.process(element, queryParameters.effectiveSearchScope, consumer)
        }
    }
}
