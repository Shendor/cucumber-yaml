package org.shendor.cucumber.yaml

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.impl.source.tree.RecursiveLighterASTNodeWalkingVisitor
import com.intellij.util.indexing.*
import org.jetbrains.plugins.cucumber.CucumberStepIndex
import org.jetbrains.yaml.YAMLElementTypes
import org.jetbrains.yaml.YAMLFileType

class YamlCucumberStepIndex : CucumberStepIndex() {
    private val inputFilter = DefaultFileTypeSpecificInputFilter(YAMLFileType.YML)

    override fun getName(): ID<Boolean, MutableList<Int>> = INDEX_ID

    override fun getVersion() = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = inputFilter

    override fun getPackagesToScan(): Array<String> = arrayOf(CucumberYamlUtil.CUCUMBER_PACKAGE)

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
                if (element.tokenType == YAMLElementTypes.KEY_VALUE_PAIR &&
                    text.substring(element.startOffset, element.endOffset).startsWith("test")) {
                    results.add(element.startOffset)
                }
                super.visitNode(element);
            }
        }
        visitor.visitNode(lighterAst.root)

        return results
    }

    companion object {
        val INDEX_ID = ID.create<Boolean, MutableList<Int>>("yaml.cucumber.step")
    }
}
