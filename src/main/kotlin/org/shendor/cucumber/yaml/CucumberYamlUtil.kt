package org.shendor.cucumber.yaml

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.shendor.cucumber.yaml.steps.YamlStepDefinition
import java.util.regex.Pattern

const val TEST_STEP_SPECIAL_CHARS_REGEX = "[\\.:>]?"

object CucumberYamlUtil {
    const val CUCUMBER_PACKAGE = "io.cucumber.java8"
    private val PARAM_REPLACEMENT_PATTERN: Pattern = Pattern.compile("<[^>]+>")

    fun isStepDefinition(candidate: YAMLKeyValue): Boolean {
        return candidate.keyText == "test"
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
        if (text.startsWith(YamlStepDefinition.REGEX_START) || text.endsWith(YamlStepDefinition.REGEX_END)) {
            return text
        }
        return "^$text$TEST_STEP_SPECIAL_CHARS_REGEX$"
    }

    fun findYamlStepDefs(module: com.intellij.openapi.module.Module): List<YAMLKeyValue> {
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

    fun findJavaUiElements(module: com.intellij.openapi.module.Module): List<PsiAnnotation> {
        val fileBasedIndex = FileBasedIndex.getInstance()
        val project = module.project

        val searchScope = module.getModuleWithDependenciesAndLibrariesScope(true)
        val files = GlobalSearchScope.getScopeRestrictedByFileTypes(searchScope, JavaFileType.INSTANCE)

        val elements = mutableListOf<PsiAnnotation>()
        val psiManager = PsiManager.getInstance(project)

        // Iterate through VirtualFiles in the scope
        fileBasedIndex.iterateIndexableFiles({ virtualFile: VirtualFile ->
            if (!virtualFile.isDirectory && files.contains(virtualFile)) {
                val psiFile = psiManager.findFile(virtualFile)
                if (psiFile != null && psiFile is PsiJavaFile) {
                    val psiClass = PsiTreeUtil.getChildrenOfType(psiFile, PsiClassImpl::class.java)!![0]
                    PsiTreeUtil.getChildrenOfType(psiClass, PsiField::class.java)?.forEach { method ->
                        PsiTreeUtil.getChildrenOfType(method, PsiModifierList::class.java)?.let { modifiers ->
                            PsiTreeUtil.getChildrenOfType(modifiers[0], PsiAnnotation::class.java)?.forEach {
                                if (it.nameReferenceElement?.text == "Location") {
                                    elements.add(it)
                                }
                            }
                        }
                    }

                }
            }
            true // continue processing
        }, project, null)

        return elements
    }
}
