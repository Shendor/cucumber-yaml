package org.chronos.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar


class YamlCompletionContributor : CompletionContributor() {

    companion object {
        val keywords = listOf(
            "test:", "parent:", "validate:", "payload:", "body:", "format:",
            "url:", "http_method:", "http_query:", "query:", "form:", "server:", "timeout:", "tls:", "username:", "password:", "headers:",
            "host:", "hosts:", "log:", "logs:", "command:", "commands:", "lookup:",
            "db_server:", "table:", "sql:", "db_command:", "expected columns:",
            "channel:", "listening_channel:", "request:", "message:", "respondTo:", "receive_timeout:", "retryDelay:", "retryCount:", "expected response:",
            "expected message:", "expected:", "assert:",
            "extractions:", "validations:",
            "validate:"
        )
        val functionsKeywords = listOf(
            "get()", "set()", "getOrElse()", "xpath()", "formatDate()", "now()", "array()", "generate()", "random()", "ftl()", "\${}", "\$()", "that()",
            "thatNot()", "thatNull()", "thatNotNull()", "jsonPath()", "regex()"
        )
    }

    override fun invokeAutoPopup(position: com.intellij.psi.PsiElement, typeChar: Char): Boolean =
        typeChar == '#' && position.containingFile is YAMLFile

    /** Track parentheses within a $(...) expression, ignoring quoted arguments. */
    internal fun insideExpression(text: String): Boolean {
        var depth = 0
        var quote: Char? = null
        var index = 0
        while (index < text.length) {
            val char = text[index]
            if (depth == 0) {
                if (char == '$' && text.getOrNull(index + 1) == '(') {
                    depth = 1
                    index++
                }
            } else if (quote != null) {
                if (char == '\\') index++
                else if (char == quote) quote = null
            } else {
                when (char) {
                    '\'', '"' -> quote = char
                    '(' -> depth++
                    ')' -> depth--
                }
            }
            index++
        }
        return depth > 0 && quote == null
    }

    private fun availableFunctions(file: PsiFile): Set<String> {
        val names = functionsKeywords.filter { it.endsWith("()") && !it.startsWith("$") }.toMutableSet()
        if (DumbService.isDumb(file.project)) return names
        val scope = ModuleUtilCore.findModuleForPsiElement(file)
            ?.getModuleWithDependenciesAndLibrariesScope(true)
            ?: GlobalSearchScope.allScope(file.project)
        PsiShortNamesCache.getInstance(file.project).getClassesByName("ProcessingFunctions", scope)
            .filter { it.isAnnotationType }
            .forEach { annotation ->
                AnnotatedElementsSearch.searchPsiMethods(annotation, scope).forEach { method ->
                    if (!method.isConstructor) names.add("${method.name}()")
                }
            }
        return names
    }

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    val parent = element.parent
                    val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java, false)
                    val beforeCaret = parameters.editor.document.charsSequence.subSequence(0, parameters.offset).toString()
                    val functionPrefix = Regex("#([a-zA-Z_][a-zA-Z_0-9]*|)$").find(beforeCaret)
                    val isScalarValue = scalar != null &&
                        ((scalar.parent as? YAMLKeyValue)?.key !== scalar)
                    val scalarPrefix = if (isScalarValue) {
                        beforeCaret.substring(scalar!!.textRange.startOffset)
                    } else {
                        // YAML treats a spaced '#' in an unquoted expression as a comment.
                        val line = beforeCaret.substringAfterLast('\n').trimStart()
                        if (line.startsWith("#")) "" else
                            Regex("^(?:[^:]+:\\s*|-\\s*)(.*)$").matchEntire(line)?.groupValues?.get(1).orEmpty()
                    }
                    if (functionPrefix != null) {
                        if (!insideExpression(scalarPrefix)) return
                        val functionsResult = result.withPrefixMatcher(
                            StartOnlyMatcher(PlainPrefixMatcher(functionPrefix.groupValues[1]))
                        )
                        availableFunctions(parameters.originalFile).forEach { name ->
                            functionsResult.addElement(LookupElementBuilder.create(name))
                        }
                        return
                    }

                    val isValue = parent is YAMLScalar && parent.parent is YAMLKeyValue
                    val keywordsToApply = if (isValue) functionsKeywords else keywords

                    var resultToUse = result
                    if (isValue) {
                        val text = parent.text
                        val offset = parameters.offset - parent.textRange.startOffset
                        
                        // Find the start of the current "word" to use as prefix
                        // We look for characters that typically separate functions/placeholders
                        var start = offset
                        while (start > 0 && !text[start - 1].isWhitespace() && text[start - 1] != '(' && text[start - 1] != ',' && text[start-1] != '+' && text[start-1] != '$'
                            && text[start-1] != '#') {
                            start--
                        }
                        
                        if (start < offset) {
                            val prefix = text.substring(start, offset)
                            resultToUse = result.withPrefixMatcher(StartOnlyMatcher(PlainPrefixMatcher(prefix)))
                        }
                    }

                    keywordsToApply
                        .forEach { 
                            resultToUse.addElement(LookupElementBuilder.create(it)) 
                        }
                }
            }
        )
    }
}