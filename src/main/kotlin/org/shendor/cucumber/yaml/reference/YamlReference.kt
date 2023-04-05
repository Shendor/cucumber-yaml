package org.shendor.cucumber.yaml.reference

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import icons.CucumberIcons
import org.jetbrains.kotlin.idea.base.util.module
import org.shendor.cucumber.yaml.CucumberYamlUtil

open class YamlReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement?>(element, textRange),
    PsiPolyVariantReference {
    protected var testName: String

    init {
        testName = element.text.substring(textRange.startOffset, textRange.endOffset)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val module = ModuleUtilCore.findModuleForPsiElement(element)
        return module?.let {
            val yamlStepDefs = CucumberYamlUtil.findYamlStepDefs(it)
            val results: MutableList<ResolveResult> = ArrayList()
            for (item in yamlStepDefs) {
                if (CucumberYamlUtil.matches(item, testName))
                    results.add(PsiElementResolveResult(item))
            }
            results.toTypedArray()
        } ?: emptyArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val variants = mutableListOf<LookupElement>()
        if (myElement != null) {
            val module = ModuleUtilCore.findModuleForPsiElement(myElement!!)
            module?.let {
                val yamlStepDefs = CucumberYamlUtil.findYamlStepDefs(module)
                for (item in yamlStepDefs) {
                    val stepName = CucumberYamlUtil.getStepName(item)
                    if (!stepName.isNullOrEmpty())
                        variants.add(
                            LookupElementBuilder
                                .create(item.text).withIcon(CucumberIcons.Cucumber)
                                .withTypeText(item.containingFile.name)
                        )
                }
            }
        }
        return variants.toTypedArray()
    }
}