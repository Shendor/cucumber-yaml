package org.shendor.cucumber.yaml

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiField
import com.intellij.psi.impl.source.tree.RecursiveLighterASTNodeWalkingVisitor
import com.intellij.util.indexing.*
import org.jetbrains.plugins.cucumber.CucumberStepIndex

class YamlToUiElementIndex : CucumberStepIndex() {
    private val inputFilter = DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE)

    override fun getName(): ID<Boolean, MutableList<Int>> = INDEX_ID

    override fun getVersion() = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = inputFilter

    override fun getPackagesToScan(): Array<String> = arrayOf()

    override fun getIndexer(): DataIndexer<Boolean, MutableList<Int>, FileContent> {
        // Override to support steps defined in subclasses
        return DataIndexer { inputData ->
            val text = inputData.contentAsText
            val lighterAst = (inputData as PsiDependentFileContent).lighterAST
            mapOf(true to getStepDefinitionOffsets(lighterAst, text))
        }
    }

    override fun getStepDefinitionOffsets(lighterAst: LighterAST, text: CharSequence): MutableList<Int> {
        val results = mutableListOf<Int>()

        val visitor = object : RecursiveLighterASTNodeWalkingVisitor(lighterAst) {
            override fun visitNode(element: LighterASTNode) {
                if (element is PsiField &&
                    text.substring(element.startOffset, element.endOffset).startsWith("name")) {
                    results.add(element.startOffset)
                }
                super.visitNode(element);
            }
        }
        visitor.visitNode(lighterAst.root)

        return results
    }

    companion object {
        val INDEX_ID = ID.create<Boolean, MutableList<Int>>("yaml.cucumber.java")
    }
}
