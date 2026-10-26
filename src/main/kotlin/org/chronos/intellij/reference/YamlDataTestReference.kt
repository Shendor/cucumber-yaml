package org.chronos.intellij.reference

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import org.chronos.intellij.search.YamlStepUsages
import org.chronos.intellij.search.YamlUsageNavigationElement
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

/** Bidirectional navigation between test/data-test values and matching name values. */
class YamlDataTestReference(element: YAMLKeyValue, range: TextRange) :
    PsiPolyVariantReferenceBase<YAMLKeyValue>(element, range, true) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = (element.value as? YAMLScalar)?.textValue ?: return emptyArray()
        if (name.isEmpty()) return emptyArray()
        val targetKeys = if (element.keyText == "name") setOf("test", "data-test") else setOf("name")
        val project = element.project
        val manager = PsiManager.getInstance(project)
        val files = FileTypeIndex.getFiles(YAMLFileType.YML, GlobalSearchScope.projectScope(project))
            .mapNotNull { manager.findFile(it) }.toMutableSet()
        // Include the current editor file even before it has been indexed.
        files.add(element.containingFile)
        val results = mutableListOf<ResolveResult>()
        for (file in files) {
            ProgressManager.checkCanceled()
            for (entry in PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)) {
                ProgressManager.checkCanceled()
                if (entry.keyText !in targetKeys) continue
                val value = entry.value as? YAMLScalar ?: continue
                if (value.textValue == name) results.add(PsiElementResolveResult(value))
            }
        }
        if (element.keyText == "test") {
            // Do not call ReferencesSearch from a resolver: its extensions may resolve this reference again.
            YamlStepUsages.process(element, GlobalSearchScope.projectScope(project), Processor { reference ->
                ProgressManager.checkCanceled()
                val usage = reference.element
                val target = when (usage) {
                    is PsiLiteralExpression, is GherkinStep -> YamlUsageNavigationElement(usage)
                    is YAMLKeyValue -> usage.value ?: usage
                    else -> usage
                }
                results.add(PsiElementResolveResult(target))
                true
            })
        }
        return results.distinctBy { it.element }.toTypedArray()
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
