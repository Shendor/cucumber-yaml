package org.chronos.intellij.reference

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.chronos.intellij.YamlCucumberExtension
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class ProjectCucumberStepReferenceTest : BasePlatformTestCase() {
    private val definitions = mutableListOf<AbstractStepDefinition>()

    override fun setUp() {
        super.setUp()
        // Exercise the real Gherkin resolver, with candidates supplied through its extension point.
        CucumberJvmExtensionPoint.EP_NAME.point.registerExtension(
            object : CucumberJvmExtensionPoint by YamlCucumberExtension() {
                override fun loadStepsFor(module: Module) = definitions
            }, testRootDisposable)
    }

    fun testLocalAndLibraryMatchOnlyNavigatesToLocalMethod() = withLibrary { library ->
        val local = localMethod()
        addDefinition(local)
        addDefinition(library)
        val ref = reference()
        assertEquals(listOf(local), ref.multiResolve(false).map { it.element })
        assertEquals(local, ref.resolve())
        assertTrue(ref.isReferenceTo(local))
        assertFalse(ref.isReferenceTo(library))
        assertEquals(listOf(local), ref.resolveToDefinitions().map { it.element })
    }

    fun testLibraryOnlyMatchIsUnresolved() = withLibrary { library ->
        addDefinition(library)
        val ref = reference()
        assertEmpty(ref.multiResolve(false))
        assertNull(ref.resolve())
        assertEmpty(ref.resolveToDefinitions())
    }

    fun testLongerLibraryPatternDoesNotHideProjectDefinition() = withLibrary { library ->
        val local = localMethod()
        addDefinition(local, "^run .*$")
        addDefinition(library)
        assertEquals(listOf(local), reference().resolveToDefinitions().map { it.element })
    }

    fun testCompletionExcludesLibrarySteps() = withLibrary { library ->
        val local = localMethod()
        addDefinition(local, "^run local$")
        addDefinition(local, "^run another$")
        addDefinition(library, "^run library$")
        myFixture.configureByText("test.feature", "Feature: Test\n  Scenario: Test\n    Given run <caret>\n")
        val items = myFixture.completeBasic().map { it.lookupString }
        assertContainsElements(items, "run local", "run another")
        assertFalse(items.contains("run library"))
    }

    fun testAttachedLibrarySourceIsExcluded() = withLibrary { _ ->
        val root = Files.createTempDirectory("cucumber-step-sources")
        try {
            val source = root.resolve("LibrarySteps.java")
            Files.writeString(source, "class LibrarySteps { void run() {} }")
            val virtualRoot = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(root)!!
            val library = ModuleRootManager.getInstance(myFixture.module).orderEntries
                .filterIsInstance<LibraryOrderEntry>().single { it.libraryName == "step-library" }.library!!
            WriteAction.run<RuntimeException> {
                val model = library.modifiableModel
                model.addRoot(virtualRoot, OrderRootType.SOURCES)
                model.commit()
            }
            val file = PsiManager.getInstance(project).findFile(
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(source)!!) as PsiJavaFile
            addDefinition(file.classes.single().methods.single())
            assertEmpty(reference().multiResolve(false))
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    fun testYamlDefinitionStillResolves() {
        val yaml = myFixture.addFileToProject("steps.yaml", "- test: run something\n")
        assertEquals(yaml, reference().resolve()!!.containingFile)
    }

    private fun localMethod(): PsiMethod =
        (myFixture.addFileToProject("Steps.java", "class Steps { void run() {} }") as PsiJavaFile).classes.single().methods.single()

    private fun addDefinition(method: PsiMethod, pattern: String = "^run something$") {
        definitions.add(object : AbstractStepDefinition(method) {
            override fun getVariableNames() = emptyList<String>()
            override fun getCucumberRegexFromElement(element: PsiElement?) = pattern
        })
    }

    private fun reference(): CucumberStepReference {
        myFixture.configureByText("test.feature", "Feature: Test\n  Scenario: Test\n    Given run <caret>something\n")
        return myFixture.file.findReferenceAt(myFixture.caretOffset) as CucumberStepReference
    }

    private fun withLibrary(action: (PsiMethod) -> Unit) {
        val root = Files.createTempDirectory("cucumber-step-library")
        try {
            val jar = root.resolve("steps.jar")
            JarOutputStream(Files.newOutputStream(jar)).use { output ->
                output.putNextEntry(JarEntry("stepdependency/LibrarySteps.class"))
                javaClass.getResourceAsStream("/stepdependency/LibrarySteps.class")!!.use { it.copyTo(output) }
                output.closeEntry()
            }
            PsiTestUtil.addLibrary(myFixture.module, "step-library", root.toString(), jar.fileName.toString())
            val library = ModuleRootManager.getInstance(myFixture.module).orderEntries
                .filterIsInstance<LibraryOrderEntry>().single { it.libraryName == "step-library" }.library!!
            try {
                val clazz = JavaPsiFacade.getInstance(project).findClass("stepdependency.LibrarySteps", GlobalSearchScope.allScope(project))!!
                action(clazz.findMethodsByName("libraryStep", false).single())
            } finally {
                PsiTestUtil.removeLibrary(myFixture.module, library)
            }
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}
