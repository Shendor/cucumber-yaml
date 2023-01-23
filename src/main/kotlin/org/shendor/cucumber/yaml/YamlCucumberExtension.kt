package org.shendor.cucumber.yaml

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import org.shendor.cucumber.yaml.steps.YamlStepDefinition
import org.shendor.cucumber.yaml.steps.YamlStepDefinitionCreator
import org.jetbrains.plugins.cucumber.BDDFrameworkType
import org.jetbrains.plugins.cucumber.StepDefinitionCreator
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLSequenceItem

class YamlCucumberExtension : AbstractCucumberExtension() {
    private val stepDefinitionCreator = YamlStepDefinitionCreator()

    override fun getStepDefinitionCreator(): StepDefinitionCreator = stepDefinitionCreator

    override fun isStepLikeFile(child: PsiElement, parent: PsiElement) = child is YAMLFile

    override fun isWritableStepLikeFile(child: PsiElement, parent: PsiElement): Boolean {
        return isStepLikeFile(child, parent) && (child as YAMLFile).virtualFile.isWritable
    }

    override fun getStepFileType() = BDDFrameworkType(YAMLFileType.YML)

    override fun getStepDefinitionContainers(featureFile: GherkinFile): MutableCollection<out PsiFile> {
        val module = ModuleUtilCore.findModuleForPsiElement(featureFile) ?: return hashSetOf()
        val stepDefinitions = loadStepsFor(featureFile, module)

        val result = hashSetOf<PsiFile>()
        stepDefinitions.forEach { stepDefinition ->
            stepDefinition.element?.let { element ->
                val psiFile = element.containingFile
                val psiDirectory = psiFile.parent
                if (psiDirectory != null && isWritableStepLikeFile(psiFile, psiDirectory)) {
                    result.add(psiFile)
                }
            }
        }

        return result
    }

    override fun loadStepsFor(featureFile: PsiFile?, module: Module): MutableList<AbstractStepDefinition> {
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
            if (CucumberYamlUtil.isStepDefinition(stepElement)) {
                YamlStepDefinition(stepElement)
            } else null
        }.toMutableList()
    }
}
