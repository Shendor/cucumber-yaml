package org.chronos.intellij.reference

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile

/** Java glue belongs to the feature's module or its project dependencies, never library roots. */
internal object CucumberStepScope {
    fun allows(feature: PsiElement, target: PsiElement?): Boolean {
        if (target?.containingFile !is PsiJavaFile) return true
        val module = ModuleUtilCore.findModuleForPsiElement(feature) ?: return false
        val file = target.containingFile?.virtualFile ?: return false
        val index = ProjectRootManager.getInstance(feature.project).fileIndex
        return module.getModuleWithDependenciesScope().contains(file) &&
            index.isInSourceContent(file) &&
            !index.isInLibraryClasses(file) && !index.isInLibrarySource(file)
    }
}
