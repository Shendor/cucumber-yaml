package org.chronos.intellij.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.AnnotatedElementsSearch

class JavaPageReference(element: PsiElement, range: TextRange, private val pageName: String) :
    PsiPolyVariantReferenceBase<PsiElement>(element, range) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        findPages().filter { it.second == pageName }
            .map { PsiElementResolveResult(it.first) }.toTypedArray()

    override fun getVariants(): Array<Any> = findPages().map { (pageClass, name) ->
        LookupElementBuilder.create(pageClass, name)
            .withIcon(pageClass.getIcon(0))
            .withTypeText(pageClass.qualifiedName)
    }.toTypedArray()

    private fun findPages(): List<Pair<PsiClass, String>> {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return emptyList()
        val project = element.project
        val scope = module.getModuleWithDependenciesAndLibrariesScope(true)
        val constantHelper = JavaPsiFacade.getInstance(project).constantEvaluationHelper
        return PsiShortNamesCache.getInstance(project).getClassesByName("PageAccessor", scope)
            .filter { it.isAnnotationType }
            .flatMap { annotationClass ->
                val qualifiedName = annotationClass.qualifiedName ?: return@flatMap emptyList()
                AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).findAll().mapNotNull { pageClass ->
                    val annotation = pageClass.modifierList?.findAnnotation(qualifiedName) ?: return@mapNotNull null
                    val value = annotation.findAttributeValue("name") ?: return@mapNotNull null
                    val name = constantHelper.computeConstantExpression(value) as? String ?: return@mapNotNull null
                    if (name.isEmpty()) null else pageClass to name
                }
            }.distinctBy { it.first }
    }
}
