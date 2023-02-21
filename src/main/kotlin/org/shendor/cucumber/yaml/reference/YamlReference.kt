package org.shendor.cucumber.yaml.reference

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import icons.CucumberIcons
import org.jetbrains.kotlin.idea.base.util.module
import org.shendor.cucumber.yaml.CucumberYamlUtil

class YamlReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement?>(element, textRange),
    PsiPolyVariantReference {
    private val testName: String

    init {
        testName = element.text.substring(textRange.startOffset, textRange.endOffset)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val module = ModuleUtilCore.findModuleForPsiElement(element)
        val yamlStepDefs = CucumberYamlUtil.findYamlStepDefs(module!!)
        val results: MutableList<ResolveResult> = ArrayList()
        for (item in yamlStepDefs) {
            if (CucumberYamlUtil.matches(item, testName))
                results.add(PsiElementResolveResult(item))
        }
        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val module = myElement!!.module!!
        val yamlStepDefs = CucumberYamlUtil.findYamlStepDefs(module)
        val variants = mutableListOf<LookupElement>()
        for (item in yamlStepDefs) {
            val stepName = CucumberYamlUtil.getStepName(item)
            if (!stepName.isNullOrEmpty())
                variants.add(
                    LookupElementBuilder
                        .create(item.text).withIcon(CucumberIcons.Cucumber)
                        .withTypeText(item.containingFile.name)
                )
        }
        return variants.toTypedArray()
    }
}