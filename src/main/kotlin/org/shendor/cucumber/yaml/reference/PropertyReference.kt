package org.shendor.cucumber.yaml.reference

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.PropertiesIcons
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import icons.CucumberIcons
import org.jetbrains.kotlin.idea.base.util.module
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.shendor.cucumber.yaml.YamlCucumberStepIndex

open class PropertyReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase<PsiElement?>(element, textRange),
    PsiPolyVariantReference {
    private var propertyName: String

    init {
        propertyName = element.text.substring(textRange.startOffset, textRange.endOffset)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val module = ModuleUtilCore.findModuleForPsiElement(element)

        val properties = findPropertyFiles(module!!)
        val results: MutableList<ResolveResult> = ArrayList()
        for (item in properties) {
            if (propertyName == item.key)
                results.add(PsiElementResolveResult(item))
        }
        return results.toTypedArray()
    }

    fun findPropertyFiles(module: com.intellij.openapi.module.Module): List<com.intellij.lang.properties.psi.Property> {
        val fileBasedIndex = FileBasedIndex.getInstance()
        val project = module.project

        val elements = mutableListOf<com.intellij.lang.properties.psi.Property>()
        val searchScope = module.getModuleWithDependenciesAndLibrariesScope(true)
            .uniteWith(ProjectScope.getLibrariesScope(project))
        val yamlFiles = GlobalSearchScope.getScopeRestrictedByFileTypes(searchScope, PropertiesFileType.INSTANCE)

        fileBasedIndex.processValues(
            YamlCucumberStepIndex.INDEX_ID,
            true,
            null,
            { file, offsets ->
                ProgressManager.checkCanceled()
                PsiManager.getInstance(project).findFile(file)?.let { psiFile ->
                    offsets.forEach { offset ->
                        val element = psiFile.findElementAt(offset + 1)
                        PsiTreeUtil.getParentOfType(element, com.intellij.lang.properties.psi.Property::class.java)?.let { propertyElement ->
                            elements.add(propertyElement)
                        }
                    }
                }
                true
            },
            yamlFiles
        )

        return elements
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val module = myElement!!.module!!
        val properties = findPropertyFiles(module)
        val variants = mutableListOf<LookupElement>()
        for (item in properties) {
            val stepName = item.key
            if (!stepName.isNullOrEmpty())
                variants.add(
                    LookupElementBuilder
                        .create(item.text).withIcon(PropertiesIcons.XmlProperties)
                        .withTypeText(item.containingFile.name)
                )
        }
        return variants.toTypedArray()
    }
}