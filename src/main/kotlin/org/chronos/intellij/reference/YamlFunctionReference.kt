package org.chronos.intellij.reference

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache

/** Resolves exact method names only when the method declares @ProcessingFunctions. */
class YamlFunctionReference(
    element: PsiElement,
    range: TextRange,
    private val functionName: String
) : PsiPolyVariantReferenceBase<PsiElement>(element, range, true) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        if (DumbService.isDumb(project)) return emptyArray()
        val scope = ModuleUtilCore.findModuleForPsiElement(element)
            ?.getModuleWithDependenciesAndLibrariesScope(true)
            ?: GlobalSearchScope.allScope(project)

        return PsiShortNamesCache.getInstance(project).getMethodsByName(functionName, scope)
            .filter { method ->
                !method.isConstructor && method.modifierList.annotations.any { annotation ->
                    annotation.qualifiedName?.substringAfterLast('.') == "ProcessingFunction"
                }
            }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
