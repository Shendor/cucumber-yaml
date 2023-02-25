package org.shendor.cucumber.yaml.steps

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLFile

class YamlStepDefinitionCreator : AbstractStepDefinitionCreator() {
    private var lastObservedLanguage = "en"

    override fun createStepDefinitionContainer(directory: PsiDirectory, name: String): PsiFile {
        val file = directory.createFile(name) as YAMLFile

//        val root = (file.documents[0] as YAMLDocument)!!
//
//        assert(key.size > 0)
//
//        var rootMapping = PsiTreeUtil.findChildOfType(
//            root,
//            YAMLMapping::class.java
//        )
//        if (rootMapping == null) {
//            val yamlFile = YAMLElementGenerator.getInstance(file.project).createDummyYamlWithText(key.get(0) + ":")
//            val mapping = ((yamlFile.documents[0] as YAMLDocument).topLevelValue as YAMLMapping?)!!
//            rootMapping = root!!.add(mapping!!) as YAMLMapping
//        }

        return file
    }

    override fun validateNewStepDefinitionFileName(project: Project, name: String): Boolean {
        return true
    }

    override fun createStepDefinition(step: GherkinStep, file: PsiFile, withTemplate: Boolean): Boolean {
        val ymlFile = (file as? YAMLFile) ?: return false

        val yamlElementGenerator = YAMLElementGenerator.getInstance(file.getProject())
        val emptySequenceItem = yamlElementGenerator.createEmptySequenceItem()
        emptySequenceItem.add(yamlElementGenerator.createYamlKeyValue("test", step.name))

        runWriteAction {
            ymlFile.add(emptySequenceItem)
        }

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

        val sourceRoots = ModuleUtilCore.findModuleForPsiElement(step)?.sourceRoots ?: return stepDir
        val root = sourceRoots.find { it.path.endsWith("resources") }
        return PsiManager.getInstance(step.project).findDirectory(root!!) ?: return stepDir
    }
}

val GherkinStep.localeLanguage: String
    get() = (this.containingFile as GherkinFile).localeLanguage
