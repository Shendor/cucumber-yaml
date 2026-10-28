package org.chronos.intellij.search

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import javax.swing.Icon

/** Icons for scalar navigation targets in Chronos YAML files. */
class YamlIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element !is YAMLScalar) return null
        val entry = element.parent as? YAMLKeyValue ?: return null
        return if (entry.keyText in setOf("test", "data-test", "name", "parent")) YAMLFileType.YML.icon else null
    }
}
