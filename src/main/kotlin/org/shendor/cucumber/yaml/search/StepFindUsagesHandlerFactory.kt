package org.shendor.cucumber.yaml.search

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement

class StepFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement): Boolean {
        return element is PomTargetPsiElement && element.target is YamlStepDeclaration
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler? {
        return object : FindUsagesHandler(element) { }
    }
}
