package org.chronos.intellij.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import org.jetbrains.yaml.YAMLHighlighter
import icons.CucumberIcons
import javax.swing.Icon

class YamlColorSettingsPage : ColorSettingsPage {
    companion object {
        val YAML_FUNCTION = TextAttributesKey.createTextAttributesKey("CHRONOS_YAML_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val YAML_NUMBER = TextAttributesKey.createTextAttributesKey("CHRONOS_YAML_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val YAML_PROPERTY_PLACEHOLDER = TextAttributesKey.createTextAttributesKey("CHRONOS_YAML_PROPERTY_PLACEHOLDER", DefaultLanguageHighlighterColors.KEYWORD)
        val YAML_PROPERTY_KEY = TextAttributesKey.createTextAttributesKey("CHRONOS_YAML_PROPERTY_KEY", DefaultLanguageHighlighterColors.IDENTIFIER)
        val YAML_PARAMETER = TextAttributesKey.createTextAttributesKey("CHRONOS_YAML_PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)
        val YAML_STRING = TextAttributesKey.createTextAttributesKey("CHRONOS_YAML_STRING", DefaultLanguageHighlighterColors.STRING)

        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Function call", YAML_FUNCTION),
            AttributesDescriptor("Number", YAML_NUMBER),
            AttributesDescriptor("Property placeholder (\${, })", YAML_PROPERTY_PLACEHOLDER),
            AttributesDescriptor("Property key", YAML_PROPERTY_KEY),
            AttributesDescriptor("Parameter (<, >)", YAML_PARAMETER),
            AttributesDescriptor("Quoted string", YAML_STRING)
        )

        private val TAG_HIGHLIGHTING = mapOf(
            "func" to YAML_FUNCTION,
            "num" to YAML_NUMBER,
            "ph" to YAML_PROPERTY_PLACEHOLDER,
            "key" to YAML_PROPERTY_KEY,
            "param" to YAML_PARAMETER,
            "str" to YAML_STRING
        )
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Chronos YAML"

    override fun getIcon(): Icon = CucumberIcons.Cucumber

    override fun getHighlighter(): SyntaxHighlighter = com.intellij.openapi.fileTypes.SyntaxHighlighterFactory.getSyntaxHighlighter(org.jetbrains.yaml.YAMLFileType.YML, null, null)!!

    override fun getDemoText(): String = """
- test: verify something
  message: <ph>${'$'}{</ph><key>msg_id</key><ph>}</ph>
  header: <str>'correlation-id-'</str> + <func>#get(</func><str>'id'</str><func>)</func>
  retries: <num>10</num> + <num>1.5</num>
  format: <func>file(</func>testdata/templates/<param><</param>filename<param>></param>.ftl<func>)</func>
""".trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = TAG_HIGHLIGHTING
}
