package org.chronos.intellij.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import org.chronos.intellij.reference.CucumberStepScope

/** Keep Cucumber's lookup text and insertion handlers while filtering Java navigation targets. */
class ProjectCucumberCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        result.runRemainingContributors(parameters) { completion ->
            if (CucumberStepScope.allows(parameters.originalFile, completion.lookupElement.psiElement)) {
                result.passResult(completion)
            }
        }
    }
}
