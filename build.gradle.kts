import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// template: https://github.com/JetBrains/intellij-platform-plugin-template/blob/main/build.gradle.kts

plugins {
    java
    kotlin("jvm") version "1.7.22"
    id("org.jetbrains.intellij") version "1.10.0"
}
val ideaVersion: String by project
val jetbrainsPublishToken: String by project

val pluginVersion: String by project

// See: https://github.com/JetBrains/gradle-intellij-plugin/ and https://github.com/JetBrains/intellij-platform-plugin-template
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
        "201.8743.12" -> "gherkin:201.8538.45"
        else -> ""
    }
//    val yamlPlugin = when (ideaVersion) {
//        "2021.1" -> "org.jetbrains.plugins.yaml:211.7142.37"
//        else -> ""
//    }
    plugins.set(
        listOf(
            "com.intellij.java",
            "org.jetbrains.plugins.yaml:223.7571.125",
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
                <li>Navigation in the source code.
              </ul>
        """
        )
        changeNotes.set(
            """
      <ul>
        <li><b>1.0.0</b> <em>(2018-03-13)</em> - Initial release</li>
      </ul>
    """
        )
    }
}
