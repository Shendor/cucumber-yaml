package org.shendor.cucumber.yaml.annotator

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.YAMLHighlighter
import org.shendor.cucumber.yaml.CucumberYamlUtil

class YamlAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiLiteralExpression) {
            return
        }

        // Ensure the Psi element contains a string that starts with the prefix and separator
        val testCaseName = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression::class.java)?.let {
            if (it.text.startsWith("runTestCase") && element.value is String)
                element.value as String? else null
        } ?: return

        val testCaseNameRange= TextRange(element.getTextRange().startOffset + 1, element.getTextRange().startOffset + testCaseName.length + 1)

        val testCases = findTestCaseDefinition(testCaseName, element)
        if (testCases.isEmpty()) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Test case definition not found")
                .range(testCaseNameRange)
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
//                .withFix(YamlCreateTestCaseQuickFix(testCaseName))
                .create()
        } else {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(testCaseNameRange).textAttributes(YAMLHighlighter.TEXT).create()
        }
    }

    fun findTestCaseDefinition(testName: String, element: PsiElement): Array<ResolveResult> {
        val module = ModuleUtilCore.findModuleForPsiElement(element)
        val yamlStepDefs = CucumberYamlUtil.findYamlStepDefs(module!!)
        val results: MutableList<ResolveResult> = ArrayList()
        for (item in yamlStepDefs) {
            if (CucumberYamlUtil.matches(item, testName))
                results.add(PsiElementResolveResult(item))
        }
        return results.toTypedArray()
    }
}