package org.chronos.intellij.reference

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.PsiTestUtil
import org.chronos.intellij.YamlCucumberExtension
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference

class CucumberStepModuleScopeTest : HeavyPlatformTestCase() {
    fun testUnrelatedModuleIsExcludedAndDependencyIsIncluded() {
        val featureRoot = getVirtualFile(createTempDir("features"))
        PsiTestUtil.addSourceContentToRoots(module, featureRoot, true)
        val featureFile = createChildData(featureRoot, "test.feature")
        setFileText(featureFile, "Feature: Test\n  Scenario: Test\n    Given run something\n")
        val feature = psiManager.findFile(featureFile)!!

        val other = createModule("other")
        val sourceRoot = getVirtualFile(createTempDir("steps"))
        PsiTestUtil.addSourceContentToRoots(other, sourceRoot, true)
        val source = createChildData(sourceRoot, "OtherSteps.java")
        setFileText(source, "class OtherSteps { void run() {} }")
        val method = (psiManager.findFile(source) as PsiJavaFile).classes.single().methods.single()
        val definition = object : AbstractStepDefinition(method) {
            override fun getVariableNames() = emptyList<String>()
            override fun getCucumberRegexFromElement(element: PsiElement?) = "^run something$"
        }
        CucumberJvmExtensionPoint.EP_NAME.point.registerExtension(
            object : CucumberJvmExtensionPoint by YamlCucumberExtension() {
                override fun loadStepsFor(module: Module) = mutableListOf(definition)
            }, testRootDisposable)

        val ref = feature.findReferenceAt(feature.text.indexOf("something")) as CucumberStepReference
        assertEmpty(ref.multiResolve(false))
        ModuleRootModificationUtil.addDependency(module, other)
        assertEquals(listOf(method), ref.multiResolve(false).map { it.element })
        assertEquals(listOf(method), ref.resolveToDefinitions().map { it.element })
    }
}
