package org.shendor.cucumber.yaml.steps

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.yaml.psi.YAMLFile

class YamlStepDefinitionCreator : AbstractStepDefinitionCreator() {
    private var lastObservedLanguage = "en"

    override fun createStepDefinitionContainer(directory: PsiDirectory, name: String): PsiFile {
        val file = directory.createFile(name) as YAMLFile

//        runWriteAction {
//            file.add(YAMLKeyValueImpl())
//        }

        return file
    }

    override fun validateNewStepDefinitionFileName(project: Project, name: String): Boolean {
        return true
    }

    override fun createStepDefinition(step: GherkinStep, file: PsiFile, withTemplate: Boolean): Boolean {
        val ymlFile = (file as? YAMLFile) ?: return false
        ymlFile.navigate(true)
        return true
    }

    override fun getDefaultStepFileName(step: GherkinStep): String {
        lastObservedLanguage = step.localeLanguage

        val basename = step.containingFile?.name?.replace(".feature", "") ?: "cucumber"
        return "${basename}-steps.yml"
    }

    override fun getDefaultStepDefinitionFolderPath(step: GherkinStep): String {
        return getDefaultYmlStepDefinitionFolderPath(step).virtualFile.path
    }

    private fun getDefaultYmlStepDefinitionFolderPath(step: GherkinStep): PsiDirectory {
        lastObservedLanguage = step.localeLanguage

        val stepDir = step.containingFile.containingDirectory

        val sourceRoots = ModuleUtilCore.findModuleForPsiElement(step)?.sourceRoots?: return stepDir
        val root = sourceRoots.find { it.path.endsWith("resources") }
        return PsiManager.getInstance(step.project).findDirectory(root!!)?: return stepDir
    }
}

val GherkinStep.localeLanguage: String
    get() = (this.containingFile as GherkinFile).localeLanguage
