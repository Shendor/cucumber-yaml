package org.shendor.cucumber.yaml

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.shendor.cucumber.yaml.steps.YamlStepDefinition
import java.util.regex.Pattern

const val TEST_STEP_SPECIAL_CHARS_REGEX = "[\\.:>]?"

object CucumberYamlUtil {
    const val CUCUMBER_PACKAGE = "io.cucumber.java8"
    private val PARAM_REPLACEMENT_PATTERN: Pattern = Pattern.compile("<[^>]+>")

    fun isStepDefinition(candidate: YAMLSequenceItem): Boolean {
        return candidate.keysValues.firstOrNull { it.keyText == "test" }?.let { true } ?: false
    }

    fun matches(testStep: YAMLSequenceItem, text: String): Boolean {
        val regex = getStepNameAsRegex(testStep)
        return Pattern.matches(regex, text)
    }

    fun getStepName(stepDefinition: YAMLSequenceItem): String? {
        val keywordExpression = stepDefinition.keysValues.firstOrNull { it.keyText == "test" }
        return keywordExpression?.value?.text
    }

    fun getStepNameAsRegex(stepDefinition: YAMLSequenceItem): String {
        var text = getStepName(stepDefinition) ?: ""
        text = PARAM_REPLACEMENT_PATTERN.matcher(text).replaceAll("(.+)")
        if (text.startsWith(YamlStepDefinition.REGEX_START) || text.endsWith(YamlStepDefinition.REGEX_END)) {
            return text
        }
        return "^$text$TEST_STEP_SPECIAL_CHARS_REGEX$"
    }

    fun findYamlStepDefs(module: com.intellij.openapi.module.Module): List<YAMLSequenceItem> {
        val fileBasedIndex = FileBasedIndex.getInstance()
        val project = module.project

        val searchScope = module.getModuleWithDependenciesAndLibrariesScope(true)
            .uniteWith(ProjectScope.getLibrariesScope(project))
        val yamlFiles = GlobalSearchScope.getScopeRestrictedByFileTypes(searchScope, YAMLFileType.YML)

        val elements = mutableListOf<YAMLSequenceItem>()
        fileBasedIndex.processValues(
            YamlCucumberStepIndex.INDEX_ID,
            true,
            null,
            { file, offsets ->
                ProgressManager.checkCanceled()
                PsiManager.getInstance(project).findFile(file)?.let { psiFile ->
                    offsets.forEach { offset ->
                        val element = psiFile.findElementAt(offset + 1)
                        PsiTreeUtil.getParentOfType(element, YAMLSequenceItem::class.java)?.let { stepElement ->
                            elements.add(stepElement)
                        }
                    }
                }
                true
            },
            yamlFiles
        )

        return elements.mapNotNull { stepElement ->
            if (isStepDefinition(stepElement)) stepElement else null
        }.toList()
    }
}
