package org.chronos.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.YAMLTokenTypes
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
            "get()", "formatDate()", "now()", "array()", "generate()", "random()", "ftl()", "\${}", "\$()", "that()",
            "thatNot()", "thatNull()", "thatNotNull()", "jsonPath()", "regex()"
        )
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