package org.chronos.intellij

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import org.chronos.intellij.steps.YamlStepDefinition
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import java.util.regex.Pattern

const val TEST_STEP_SPECIAL_CHARS_REGEX = "[\\.:>]?"

private const val UI_LOCATOR_ANNOTATION_NAME = "Locator"

object CucumberYamlUtil {
    const val CUCUMBER_PACKAGE = "io.cucumber.java8"
    private val DEFAULT_VALUE_SEPARATOR = Regex(":")
    private val PARAM_REPLACEMENT_PATTERN: Pattern = Pattern.compile("<[^>]+>")

    fun isStepDefinition(candidate: YAMLKeyValue): Boolean {
        return candidate.keyText == "test" && candidate.value is YAMLScalar
    }

    fun matches(testStep: YAMLKeyValue, text: String): Boolean {
        val regex = getStepNameAsRegex(testStep)
        return Pattern.matches(regex, text)
    }

    fun getStepName(stepDefinition: YAMLKeyValue): String {
        return stepDefinition.valueText
    }

    fun getStepNameAsRegex(stepDefinition: YAMLKeyValue): String {
        var text = getStepName(stepDefinition) ?: ""
        text = PARAM_REPLACEMENT_PATTERN.matcher(text).replaceAll("(.+)")
        if (text.startsWith(YamlStepDefinition.Companion.REGEX_START) || text.endsWith(YamlStepDefinition.Companion.REGEX_END)) {
            return text
        }
        return "^$text$TEST_STEP_SPECIAL_CHARS_REGEX$"
    }

    fun findYamlStepDefs(module: Module): List<YAMLKeyValue> {
        val fileBasedIndex = FileBasedIndex.getInstance()
        val project = module.project

        val searchScope = module.getModuleWithDependenciesAndLibrariesScope(true)
            .uniteWith(ProjectScope.getLibrariesScope(project))
        val yamlFiles = GlobalSearchScope.getScopeRestrictedByFileTypes(searchScope, YAMLFileType.YML)

        val elements = mutableListOf<YAMLKeyValue>()
        fileBasedIndex.processValues(
            YamlCucumberStepIndex.INDEX_ID,
            true,
            null,
            { file, offsets ->
                ProgressManager.checkCanceled()
                PsiManager.getInstance(project).findFile(file)?.let { psiFile ->
                    offsets.forEach { offset ->
                        val element = psiFile.findElementAt(offset + 1)
                        PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java)?.let { keyValue ->
                            if (isStepDefinition(keyValue)) elements.add(keyValue)
                        }
                    }
                }
                true
            },
            yamlFiles
        )

        return elements.toList()
    }

    fun findJavaUiElements(module: Module): List<PsiAnnotation> {
        val scope = module.getModuleWithDependenciesAndLibrariesScope(true)
        return PsiShortNamesCache.getInstance(module.project).getClassesByName(UI_LOCATOR_ANNOTATION_NAME, scope)
            .filter { it.isAnnotationType }
            .flatMap { annotationClass ->
                val qualifiedName = annotationClass.qualifiedName ?: return@flatMap emptyList()
                AnnotatedElementsSearch.searchPsiFields(annotationClass, scope).findAll().mapNotNull { field ->
                    field.modifierList?.findAnnotation(qualifiedName)
                }
            }.distinct()
    }

    fun extractToKeyAndDefaultValue(key: String): List<String> {
        return DEFAULT_VALUE_SEPARATOR.split(key)
    }
}
