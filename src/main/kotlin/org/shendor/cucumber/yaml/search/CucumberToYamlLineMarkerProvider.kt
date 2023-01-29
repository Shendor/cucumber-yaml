package org.shendor.cucumber.yaml.search

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.JBColor
import icons.CucumberIcons
import com.intellij.util.Processor
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference
import org.jetbrains.plugins.cucumber.steps.search.CucumberStepSearchUtil
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.jetbrains.yaml.psi.YAMLSequenceItem

class CucumberToYamlLineMarkerProvider : LineMarkerProvider {

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        elements
            .filterIsInstance<YAMLSequenceItem>()
            .forEach { token ->

                // Check context
//                val annotation = PsiTreeUtil.getParentOfType(token, PsiAnnotation::class.java) ?: return@forEach
//                if (annotation.resolveAnnotationType()?.qualifiedName?.startsWith("io.cucumber.java") != true) return@forEach
//                val annotationText = (token.parent as? PsiLiteralExpression)?.value as? String ?: return@forEach
//                val method = PsiTreeUtil.getParentOfType(token, PsiMethod::class.java) ?: return@forEach
                if (CucumberYamlUtil.isStepDefinition(token)) {
                    // Find method usages
                    DumbService.getInstance(token.project).runReadActionInSmartMode {
                        val usages = findStepUsages(token)
                        if (usages.isNotEmpty())
                            buildMarkers(token, usages, CucumberYamlUtil.getStepNameAsRegex(token), result)
                    }
                }
            }
    }

    protected fun buildMarkers(
        token: PsiElement,
        usages: List<PsiReference>,
        annotationText: String?,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        // Group usages by regex
        val groupedByRegex: Map<String, List<GherkinStep>> = usages.map { it.element }
            .filterIsInstance<GherkinStep>()
            .flatMap { step ->
                step.findDefinitions()
                    .toSet()
                    .mapNotNull { it.expression }
                    .map { it to step }
            }
            .groupBy { it.first }
            .map { it.key to it.value.map { it.second }.toList() }
            .toMap()

        // Find annotation usages
        val steps = groupedByRegex[annotationText]
            ?: return

        // Add marker
        result.add(
            buildMarker(
                element = token,
                targets = steps.map { it.stepHolder })
        )
    }

    protected fun buildMarker(
        element: PsiElement,
        targets: List<GherkinStepsHolder>
    ): RelatedItemLineMarkerInfo<PsiElement> {
        val toSet = targets.toSet()
        val usagesCount = toSet.size
        val usagesText = if (usagesCount == 1) "Used by 1 scenario" else "Used by $usagesCount scenarios"

        val builder = NavigationGutterIconBuilder
            .create(getNumberIcon(usagesCount, JBColor.foreground()))
            .setTargets(toSet)
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .setTooltipText(usagesText)
            .setPopupTitle("Cucumber")

        return builder.createLineMarkerInfo(element)
    }

    fun findStepUsages(element: PsiElement): List<PsiReference> {

        val scope = CucumberStepSearchUtil.restrictScopeToGherkinFiles(GlobalSearchScope.projectScope(element.project))
        val search = ReferencesSearch.search(element, scope, false)

        val references = mutableListOf<PsiReference>()
        search.forEach(Processor { ref: PsiReference ->
            val elt = ref.element
            if (elt is GherkinStep && ref is CucumberStepReference)
                references.add(ref)
            true
        })

        return references
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
//        if (element is YAMLSequenceItem && CucumberYamlUtil.isStepDefinition(element)) {
//            return createMarker(element, CucumberYamlUtil.getStepName(element))
//        }
        return null
    }

    private fun createMarker(element: PsiElement, toolTip: String?): LineMarkerInfo<*> {
        val anchor = PsiTreeUtil.getDeepestFirst(element)
        return LineMarkerInfo(
            anchor,
            anchor.textRange,
            CucumberIcons.Cucumber,
            { toolTip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { "" }
        )
    }
}
