import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

repositories {
    mavenCentral()
}

plugins {
    java
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.intellij") version "1.17.3"
}
val ideaVersion: String by project
val jetbrainsPublishToken: String by project

val pluginVersion: String by project

intellij {
    pluginName.set("cucumber-yaml")
    version.set(ideaVersion)
    plugins.set(
        listOf(
            "com.intellij.java",
            "org.jetbrains.plugins.yaml:242.20224.237",
//            "com.intellij.properties:223.7571.117",
//            "Kotlin",
            "gherkin:242.20224.159"
        )
    )
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
//        token.set(jetbrainsPublishToken)
        channels.set(listOf(version.toString().split('-').getOrElse(1) { "default" }.split('.').first()))
    }
    register<Exec>("publishTag") {
        dependsOn(publishPlugin)
        commandLine = listOf("git", "push", "origin", version.toString())
    }
    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("")
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
        <li><b>1.0.0</b> <em>(2025-20-01)</em> - New YML-Cucumber and UI Elements references</li>
      </ul>
    """
        )
    }
}
