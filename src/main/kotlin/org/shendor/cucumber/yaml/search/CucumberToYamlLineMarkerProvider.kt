package org.shendor.cucumber.yaml.search

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import icons.CucumberIcons
import org.shendor.cucumber.yaml.CucumberYamlUtil
import org.jetbrains.yaml.psi.YAMLSequenceItem

class CucumberToYamlLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element is YAMLSequenceItem && CucumberYamlUtil.isStepDefinition(element)) {
            return createMarker(element, CucumberYamlUtil.getStepName(element))
        }
        return null
    }

    private fun createMarker(element: PsiElement, toolTip: String?): LineMarkerInfo<*> {
        val anchor = PsiTreeUtil.getDeepestFirst(element)
        return LineMarkerInfo(
            anchor,
            anchor.textRange,
            CucumberIcons.Cucumber,
            { toolTip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { "" }
        )
    }
}
