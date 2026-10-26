package org.chronos.intellij.search

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import org.jetbrains.yaml.YAMLFileType

class JavaUiUseScopeEnlarger : UseScopeEnlarger() {
    override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
        JavaUiUsageSearcher.declarationOwner(element) ?: return null
        // YAML may refer to private fields and package-private page classes by their annotation names.
        return GlobalSearchScope.getScopeRestrictedByFileTypes(
            GlobalSearchScope.projectScope(element.project), YAMLFileType.YML)
    }
}
