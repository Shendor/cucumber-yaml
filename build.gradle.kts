import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.intellij") version "1.15.0"
}
val ideaVersion: String by project
val jetbrainsPublishToken: String by project

val pluginVersion: String by project

intellij {
    pluginName.set("cucumber-yaml")
    version.set(ideaVersion)
    type.set("IC")
    downloadSources.set(true)
    instrumentCode.set(true)

    // Gherkin plugin version: https://plugins.jetbrains.com/plugin/9164-gherkin/versions
    val gherkinPlugin = when (ideaVersion) {
        "2020.2" -> "gherkin:202.6397.21"
        "2020.3" -> "gherkin:203.5981.155"
        "2021.1" -> "gherkin:211.6693.111"
        "2021.2" -> "gherkin:212.4746.57"
        "2021.3" -> "gherkin:213.5744.223"
        "2022.1" -> "gherkin:221.5080.126"
        "2022.2" -> "gherkin:222.3345.118"
        "2022.3" -> "gherkin:223.7571.113"
        "2023.1" -> "gherkin:231.8109.91"
        "2023.2" -> "gherkin:232.8660.88"
        "2023.3" -> "gherkin:233.11799.165"
        "201.8743.12" -> "gherkin:202.6397.21"
        else -> ""
    }
    val yamlPlugin = when (ideaVersion) {
        "2020.2" -> "org.jetbrains.plugins.yaml:202.6397.21"
        "2020.3" -> "org.jetbrains.plugins.yaml:203.5981.37"
        "2021.1" -> "org.jetbrains.plugins.yaml:211.6693.44"
        "2021.2" -> "org.jetbrains.plugins.yaml:212.4746.16"
        "2021.3" -> "org.jetbrains.plugins.yaml:213.5744.9"
        "2022.1" -> "org.jetbrains.plugins.yaml:221.5080.106"
        "2022.2" -> "org.jetbrains.plugins.yaml:222.3345.35"
        "2022.3" -> "org.jetbrains.plugins.yaml:223.7571.59"
        "2023.1" -> "org.jetbrains.plugins.yaml:231.8109.126"
        "2023.2" -> "org.jetbrains.plugins.yaml:232.8660.88"
        "2023.3" -> "org.jetbrains.plugins.yaml:233.11799.165"
        else -> ""
    }

    plugins.set(
        listOf(
            "com.intellij.java",
            yamlPlugin,
//            "com.intellij.properties:223.7571.117",
            "Kotlin",
            gherkinPlugin
        )
    )
}

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/snapshots")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("io.cucumber:cucumber-java:7.2.3")
}

tasks {
    register<Exec>("tag") {
        commandLine = listOf("git", "tag", version.toString())
    }
    publishPlugin {
        dependsOn("tag")
        token.set(jetbrainsPublishToken)
        channels.set(listOf(version.toString().split('-').getOrElse(1) { "default" }.split('.').first()))
    }
    register<Exec>("publishTag") {
        dependsOn(publishPlugin)
        commandLine = listOf("git", "push", "origin", version.toString())
    }
    patchPluginXml {
        pluginDescription.set(
            """
              <p>
                This plugin enables <a href="https://cucumber.io/">Cucumber</a> support with step definitions written in Yaml.
              </p>
              <p>
                The following coding assistance features are available:
              </p>
              <ul>
                <li>Navigation from Cucumber feature file to YAML.
                <li>Navigation from Java step def to YAML.
                <li>Navigation from YAML to Cucumber.
                <li>Documentation in tooltips.
                <li>Keywords autocomplete.
              </ul>
        """
        )
        changeNotes.set(
            """
      <ul>
        <li><b>1.0.0</b> <em>(2023-01-04)</em> - Initial release</li>
      </ul>
    """
        )
    }
}
