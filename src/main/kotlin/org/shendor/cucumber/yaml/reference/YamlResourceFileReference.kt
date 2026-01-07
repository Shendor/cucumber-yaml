package org.shendor.cucumber.yaml.reference

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

class YamlResourceFileReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val filePath: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val results = mutableListOf<ResolveResult>()

        val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
        
        val fileName = filePath.substringAfterLast('/')
        val files = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.projectScope(project))
        for (file in files) {
            val virtualFile = file.virtualFile
            if (virtualFile != null) {
                val normalizedPath = virtualFile.path.replace("\\", "/")
                val normalizedFilePath = filePath.replace("\\", "/")
                if (normalizedPath.endsWith(normalizedFilePath)) {
                    // Check if it's in a source root (which includes resources)
                    if (projectFileIndex.isInSource(virtualFile) || projectFileIndex.isInSourceContent(virtualFile)) {
                        results.add(PsiElementResolveResult(file))
                    }
                }
            }
        }

        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.isNotEmpty()) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }
}
