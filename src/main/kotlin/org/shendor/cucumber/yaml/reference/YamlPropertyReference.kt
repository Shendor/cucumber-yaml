package org.shendor.cucumber.yaml.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

class YamlPropertyReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val propertyKey: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val properties = findProperties(project, propertyKey)
        return properties.map { PsiElementResolveResult(it.psiElement) }.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val properties = findAllProperties(project)
        return properties.map { property ->
            val psiElement = property.psiElement
            LookupElementBuilder.create(property.key ?: "")
                .withIcon(psiElement.getIcon(0))
                .withTypeText(psiElement.containingFile.name)
        }.toTypedArray()
    }

    private fun findProperties(project: Project, key: String): List<IProperty> {
        val result = mutableListOf<IProperty>()
        val virtualFiles = FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, GlobalSearchScope.allScope(project))
        val psiManager = PsiManager.getInstance(project)
        for (virtualFile in virtualFiles) {
            val propertiesFile = psiManager.findFile(virtualFile) as? PropertiesFile ?: continue
            propertiesFile.findPropertyByKey(key)?.let { result.add(it) }
        }
        return result
    }

    private fun findAllProperties(project: Project): List<IProperty> {
        val result = mutableListOf<IProperty>()
        val virtualFiles = FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, GlobalSearchScope.allScope(project))
        val psiManager = PsiManager.getInstance(project)
        for (virtualFile in virtualFiles) {
            val propertiesFile = psiManager.findFile(virtualFile) as? PropertiesFile ?: continue
            result.addAll(propertiesFile.properties)
        }
        return result
    }
}
