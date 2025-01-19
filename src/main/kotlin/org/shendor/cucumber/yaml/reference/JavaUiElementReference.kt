package org.shendor.cucumber.yaml.reference

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import icons.CucumberIcons
import org.shendor.cucumber.yaml.CucumberYamlUtil

open class JavaUiElementReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement?>(element, textRange),
    PsiPolyVariantReference {
    protected var uiElementName: String

    init {
        uiElementName = element.text.substring(textRange.startOffset, textRange.endOffset)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val module = ModuleUtilCore.findModuleForPsiElement(element)
        return module?.let {
            val uiElements = CucumberYamlUtil.findJavaUiElements(it)
            val results: MutableList<ResolveResult> = ArrayList()
            for (item in uiElements) {
                if (getLocationName(item) == uiElementName)
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
                val uiElements = CucumberYamlUtil.findJavaUiElements(module)
                for (item in uiElements) {
                    if (getLocationName(item) == uiElementName) {
                        variants.add(
                            LookupElementBuilder
                                .create(item.text).withIcon(CucumberIcons.Cucumber)
                                .withTypeText(item.containingFile.name)
                        )
                    }
                }
            }
        }
        return variants.toTypedArray()
    }

    private fun getLocationName(element: PsiElement): String {
        return when (element) {
            is PsiLiteralExpression -> element.value?.toString() ?: ""
            is PsiAnnotation -> {
                PsiTreeUtil.getChildrenOfType(element, PsiAnnotationParameterList::class.java)?.first()?.let { paramList ->
                    PsiTreeUtil.getChildrenOfType(paramList, PsiNameValuePair::class.java)
                        ?.find { pair -> pair.text.startsWith("name") || pair.children.isNotEmpty() }
                        ?.let {
                            extractTextFromJavaString(PsiTreeUtil.getChildrenOfType(it, PsiLiteralExpression::class.java)?.first())
                        } ?: ""
                } ?: ""
            }
            else -> ""
        }
    }

    private fun extractTextFromJavaString(element: PsiLiteralExpression?): String {
        return if (element?.text?.startsWith("\"") == true) element.text.substring(1, element.text.length - 1) else element?.text ?: ""
    }
}