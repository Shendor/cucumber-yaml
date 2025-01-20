package org.shendor.cucumber.yaml.search

import com.intellij.openapi.progress.ProgressManager
import com.intellij.pom.PomDeclarationSearcher
import com.intellij.pom.PomTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Consumer
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import org.shendor.cucumber.yaml.CucumberYamlUtil

class StepDeclarationSearcher : PomDeclarationSearcher() {
    override fun findDeclarationsAt(element: PsiElement, offsetInElement: Int, consumer: Consumer<in PomTarget>) {
        ProgressManager.checkCanceled()
        if (element is YAMLScalar && (element.parent as? YAMLKeyValue)?.keyText == "test") {
            val stepDeclaration = findStepDeclaration(element.parent as YAMLKeyValue)

            stepDeclaration?.let {
                consumer.consume(it)
            }
        }
    }

    private fun findStepDeclaration(element: YAMLKeyValue): YamlStepDeclaration? {
        val stepName = CucumberYamlUtil.getStepName(element)
        return getStepDeclaration(element, stepName)
    }

    private fun getStepDeclaration(element: PsiElement, stepName: String?): YamlStepDeclaration? {
        if (stepName == null) {
            return null
        }
        return CachedValuesManager.getCachedValue(element) {
            CachedValueProvider.Result.create(YamlStepDeclaration(element, stepName), element)
        }
    }
}
