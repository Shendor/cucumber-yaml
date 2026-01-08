package org.chronos.intellij.search

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import org.chronos.intellij.CucumberYamlUtil
import org.jetbrains.plugins.cucumber.CucumberUtil
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue

class StepDefinitionUsageSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: ReferencesSearch.SearchParameters,
        consumer: Processor<in PsiReference>
    ) {
        val elementToSearch = queryParameters.elementToSearch
        var element: PsiElement = queryParameters.elementToSearch
        if (elementToSearch is PomTargetPsiElement) {
            val declaration = elementToSearch.target
            if (declaration is YamlStepDeclaration) {
                element = declaration.element
            }
        }

        if (element is YAMLKeyValue) {
            val stepName = CucumberYamlUtil.getStepName(element)
            val regex = CucumberYamlUtil.getStepNameAsRegex(element)

            inReadAction {
                // Search in Gherkin files
                CucumberUtil.findGherkinReferencesToElement(
                    element,
                    regex,
                    consumer,
                    queryParameters.effectiveSearchScope
                )

                // Search in YAML files for 'parent' references
                if (stepName != null) {
                    val project = element.project
                    val scope = queryParameters.effectiveSearchScope
                    
                    // We can use ReferencesSearch.search to find references that resolve to this element.
                    // However, we want to specifically ensure our YamlReferences are found.
                    // Since they are not indexed by the platform's default word index as references to this element,
                    // we might need to find them by searching for the text.
                    
                    // Actually, if we just want to find usages of the string in YAML files:
                    val searchScope = if (scope is GlobalSearchScope) scope else GlobalSearchScope.allScope(project)
                    val yamlFiles = FileTypeIndex.getFiles(YAMLFileType.YML, searchScope)
                    for (file in yamlFiles) {
                        val psiFile = com.intellij.psi.PsiManager.getInstance(project).findFile(file) ?: continue
                        psiFile.accept(object : com.intellij.psi.PsiRecursiveElementWalkingVisitor() {
                            override fun visitElement(elementInFile: PsiElement) {
                                if (elementInFile is YAMLKeyValue && elementInFile.keyText == "parent") {
                                    for (reference in elementInFile.references) {
                                        if (reference.isReferenceTo(element)) {
                                            consumer.process(reference)
                                        }
                                    }
                                }
                                super.visitElement(elementInFile)
                            }
                        })
                    }
                }
            }
        }
    }

    private fun <T> inReadAction(body: () -> T): T {
        return ApplicationManager.getApplication().run {
            if (isReadAccessAllowed) {
                body()
            } else runReadAction<T>(body)
        }
    }

}
