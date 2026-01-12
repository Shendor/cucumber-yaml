package org.chronos.intellij.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.chronos.intellij.highlighting.YamlColorSettingsPage
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

class YamlPropertyKeyAnnotator : Annotator {
    private val regex = Regex("\\\$\\{([^}]+)}")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLScalar) return

        val elementText = element.text
        val matches = regex.findAll(elementText)
        val project = element.project

        for (match in matches) {
            val group = match.groups[1] ?: continue
            val propertyKey = group.value
            val start = element.textRange.startOffset + group.range.first
            val end = element.textRange.startOffset + group.range.last + 1
            val range = TextRange(start, end)

            val exists = propertyExists(project, propertyKey)

            if (!exists) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Property '$propertyKey' not found")
                    .range(range)
                    .create()
            } else {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range)
                    .textAttributes(YamlColorSettingsPage.YAML_PROPERTY_KEY)
                    .create()
            }
        }
    }

    private fun propertyExists(project: Project, key: String): Boolean {
        // Check in .properties files
        val propertiesFiles = FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, GlobalSearchScope.allScope(project))
        val psiManager = PsiManager.getInstance(project)
        for (virtualFile in propertiesFiles) {
            val propertiesFile = psiManager.findFile(virtualFile) as? PropertiesFile ?: continue
            if (propertiesFile.findPropertyByKey(key) != null) return true
        }

        // Check in .yaml files
        val yamlFiles = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.allScope(project))
        for (virtualFile in yamlFiles) {
            val yamlFile = psiManager.findFile(virtualFile) as? YAMLFile ?: continue
            if (findYamlElement(yamlFile, key) != null) return true
        }

        return false
    }

    private fun findYamlElement(yamlFile: YAMLFile, key: String): PsiElement? {
        val keys = key.split(".")
        for (doc in yamlFile.documents) {
            val topLevelValue = doc.topLevelValue as? YAMLMapping ?: continue
            val found = findNestedElement(topLevelValue, keys, 0)
            if (found != null) return found
        }
        return null
    }

    private fun findNestedElement(mapping: YAMLMapping, keys: List<String>, index: Int): PsiElement? {
        if (index >= keys.size) return null
        val keyValue = mapping.getKeyValueByKey(keys[index]) ?: return null
        if (index == keys.size - 1) return keyValue.key
        val nextMapping = keyValue.value as? YAMLMapping ?: return null
        return findNestedElement(nextMapping, keys, index + 1)
    }
}
