package org.chronos.intellij.search

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import icons.CucumberIcons
import org.chronos.intellij.reference.JavaPageReference
import org.chronos.intellij.reference.JavaUiElementReference
import org.jetbrains.yaml.psi.YAMLKeyValue

class JavaUiUsageLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        for (element in elements) {
            if (element !is PsiIdentifier || DumbService.isDumb(element.project)) continue
            val reference = element.parent as? PsiJavaCodeReferenceElement ?: continue
            val annotation = reference.parent as? PsiAnnotation ?: continue
            val owner = JavaUiUsageSearcher.declarationOwner(annotation) ?: continue
            val expectedName = if (owner is PsiClass) "PageAccessor" else "Locator"
            if (reference.referenceName != expectedName) continue
            val targets = findUsageTargets(owner)
            if (targets.isEmpty()) continue
            result.add(NavigationGutterIconBuilder.create(CucumberIcons.Cucumber)
                .setTargets(targets)
                .setTooltipText("Chronos YAML usages (${targets.size})")
                .setPopupTitle("Chronos YAML usages")
                .createLineMarkerInfo(element))
        }
    }

    fun findUsageTargets(element: PsiElement): List<PsiElement> =
        ReferencesSearch.search(element, GlobalSearchScope.projectScope(element.project)).findAll()
            .filter { it is JavaPageReference || it is JavaUiElementReference }
            .mapNotNull { (it.element as? YAMLKeyValue)?.value }
            .map { PsiTreeUtil.getDeepestFirst(it) }.distinct()
}
