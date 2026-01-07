package org.shendor.cucumber.yaml.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class YamlToYamlPropertyReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val propertyKey: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val results = mutableListOf<ResolveResult>()
        
        val yamlFiles = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.allScope(project))
        val psiManager = PsiManager.getInstance(project)
        
        for (virtualFile in yamlFiles) {
            val yamlFile = psiManager.findFile(virtualFile) as? YAMLFile ?: continue
            val foundElement = findYamlElement(yamlFile, propertyKey)
            if (foundElement != null) {
                results.add(PsiElementResolveResult(foundElement))
            }
        }
        
        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        // Variants for YAML keys could be quite complex, skipping for now or implementing basic ones if needed.
        // For now, let's keep it simple or return empty like YamlResourceFileReference did.
        // Actually, YamlPropertyReference implemented it. Let's try to provide some variants.
        val project = element.project
        val variants = mutableListOf<Any>()
        val yamlFiles = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.allScope(project))
        val psiManager = PsiManager.getInstance(project)

        for (virtualFile in yamlFiles) {
            val yamlFile = psiManager.findFile(virtualFile) as? YAMLFile ?: continue
            collectAllKeys(yamlFile, "", variants)
        }
        return variants.toTypedArray()
    }

    private fun findYamlElement(yamlFile: YAMLFile, key: String): PsiElement? {
        val keys = key.split(".")
        var current: PsiElement? = null
        
        for (doc in yamlFile.documents) {
            val topLevelValue = doc.topLevelValue as? YAMLMapping ?: continue
            current = findNestedElement(topLevelValue, keys, 0)
            if (current != null) return current
        }
        return null
    }

    private fun findNestedElement(mapping: YAMLMapping, keys: List<String>, index: Int): PsiElement? {
        if (index >= keys.size) return null
        
        val keyValue = mapping.getKeyValueByKey(keys[index]) ?: return null
        
        if (index == keys.size - 1) {
            return keyValue.key // Navigate to the key
        }
        
        val nextMapping = keyValue.value as? YAMLMapping ?: return null
        return findNestedElement(nextMapping, keys, index + 1)
    }

    private fun collectAllKeys(yamlFile: YAMLFile, prefix: String, variants: MutableList<Any>) {
        for (doc in yamlFile.documents) {
            val topLevelValue = doc.topLevelValue as? YAMLMapping ?: continue
            collectKeysFromMapping(topLevelValue, prefix, variants)
        }
    }

    private fun collectKeysFromMapping(mapping: YAMLMapping, prefix: String, variants: MutableList<Any>) {
        for (keyValue in mapping.keyValues) {
            val key = keyValue.keyText
            if (key.isEmpty()) continue
            
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            variants.add(
                LookupElementBuilder.create(fullKey)
                    .withIcon(keyValue.getIcon(0))
                    .withTypeText(keyValue.containingFile.name)
            )
            
            val nextMapping = keyValue.value as? YAMLMapping
            if (nextMapping != null) {
                collectKeysFromMapping(nextMapping, fullKey, variants)
            }
        }
    }
}
