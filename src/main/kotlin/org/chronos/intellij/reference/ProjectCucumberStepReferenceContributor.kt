package org.chronos.intellij.reference

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.plugins.cucumber.steps.CucumberStepHelper
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReferenceProvider

class ProjectCucumberStepReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(GherkinStep::class.java),
            object : PsiReferenceProvider() {
                private val delegate = CucumberStepReferenceProvider()

                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
                    delegate.getReferencesByElement(element, context).map { reference ->
                        ProjectCucumberStepReference(element, reference.rangeInElement)
                    }.toTypedArray()
            },
            // The same range at higher priority suppresses the unfiltered built-in reference.
            PsiReferenceRegistrar.HIGHER_PRIORITY
        )
    }
}

private class ProjectCucumberStepReference(element: PsiElement, range: TextRange) :
    CucumberStepReference(element, range) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        super.multiResolve(incompleteCode).filter { CucumberStepScope.allows(element, it.element) }.toTypedArray()

    override fun resolveToDefinitions(): Collection<AbstractStepDefinition> {
        val step = element as GherkinStep
        val module = ModuleUtilCore.findModuleForPsiElement(step) ?: return emptyList()
        val name = step.substitutedName ?: return emptyList()
        // Filter before Cucumber's longest-pattern-per-definition-type selection, otherwise
        // a longer library pattern can hide an eligible project definition.
        return CucumberStepHelper.loadStepsFor(module)
            .filter { CucumberStepScope.allows(step, it.element) && it.supportsStep(step) && it.matches(name) }
            .groupBy { it.javaClass }
            .values.map { matches -> matches.maxBy { it.pattern?.pattern()?.length ?: -1 } }
    }
}
