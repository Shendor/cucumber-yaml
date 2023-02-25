package org.shendor.cucumber.yaml.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.YAMLTokenTypes


class YamlCompletionContributor : CompletionContributor() {

    companion object {
        val keywords = listOf("test:", "validate:", "payload:", "body:", "url:", "http_method:", "query:", "form:", "server:",
            "host:", "hosts:", "log:", "logs:", "command:", "commands:", "db_server:", "table:", "sql:", "channel:",
            "listening_channel:", "request:", "message:", "respondTo:", "timeout:", "delay:", "attempts:", "expected response:",
            "expected message:", "expected:",
            "assert:",
            "contains()", "regex()", "get()", "setAndGet()", "formatDate()", "now()", "validate():",
            "validate(json_path()):", "validate(xPath()):")
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
                    keywords.filter { it.startsWith(currentText) }
                        .forEach { result.addElement(LookupElementBuilder.create(it)) }
                }
            }
        )
    }
}