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

        val lastSlashIndex = filePath.lastIndexOf('/')
        val directoryPath = if (lastSlashIndex != -1) filePath.substring(0, lastSlashIndex) else ""
        val fileNamePattern = if (lastSlashIndex != -1) filePath.substring(lastSlashIndex + 1) else filePath

        // Convert pattern to regex:
        // 1. Escape dots
        // 2. Replace <...> with .+
        val regexString = fileNamePattern
            .replace(".", "\\.")
            .replace(Regex("<[^>]+>"), ".+")
        val fileNameRegex = Regex("^$regexString$")

        val files = mutableListOf<PsiFile>()
        FilenameIndex.processAllFileNames({ fileName ->
            if (fileNameRegex.matches(fileName)) {
                val foundFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.projectScope(project))
                files.addAll(foundFiles)
            }
            true
        }, GlobalSearchScope.projectScope(project), null)

        for (file in files) {
            val virtualFile = file.virtualFile
            if (virtualFile != null) {
                val normalizedPath = virtualFile.path.replace("\\", "/")
                val normalizedFilePath = filePath.replace("\\", "/")
                
                // If there's a directory path, check if the file is in that directory
                val matchesPath = if (directoryPath.isNotEmpty()) {
                    val normalizedDirectoryPath = directoryPath.replace("\\", "/")
                    normalizedPath.contains("/$normalizedDirectoryPath/") || normalizedPath.startsWith("$normalizedDirectoryPath/")
                } else {
                    true
                }

                if (matchesPath) {
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
