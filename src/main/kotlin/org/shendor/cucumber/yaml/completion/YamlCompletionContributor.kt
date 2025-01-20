package org.shendor.cucumber.yaml.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLKeyValue


class YamlCompletionContributor : CompletionContributor() {

    companion object {
        val keywords = listOf(
            "test:", "parent", "validate:", "payload:", "body:", "format:",
            "url:", "http_method:", "query:", "form:", "server:", "timeout:", "tls:", "username:", "password:", "headers:",
            "host:", "hosts:", "log:", "logs:", "command:", "commands:", "lookup:",
            "db_server:", "table:", "sql:", "db_command:", "expected columns:",
            "channel:", "listening_channel:", "request:", "message:", "respondTo:", "receive_timeout:", "delay:", "attempts:", "expected response:",
            "expected message:", "expected:", "assert:",
            "extractions:", "validations:",
            "validate():", "validate(json_path()):", "validate(xpath()):"
        )
        val functionsKeywords = listOf(
            "contains()", "regex()", "get()", "not()", "setAndGet()", "hasSize(1)", "formatDate()", "now()",
            "generate()", "increment()", "map()", "list()", "array()", "not()", "encode()"
        )
    }

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.TEXT),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val currentText = parameters.originalPosition?.text ?: ""
                    val keywordsToApply = if (parameters.originalPosition?.parent?.parent !is YAMLKeyValue) keywords
                    else functionsKeywords

                    keywordsToApply.filter { it.startsWith(currentText) }
                        .forEach { result.addElement(LookupElementBuilder.create(it)) }
                }
            }
        )
    }
}