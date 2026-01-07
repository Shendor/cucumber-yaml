import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    gradlePluginPortal()
    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    java
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

apply(plugin = "org.jetbrains.intellij.platform")

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        name.set("cucumber-yaml")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

dependencies {
    implementation("io.cucumber:cucumber-java:7.2.3")
    implementation("org.jetbrains.intellij.platform:org.jetbrains.intellij.platform.gradle.plugin:2.10.5")

    intellijPlatform {
        create(
            type = providers.gradleProperty("platformType"),
            version = providers.gradleProperty("platformVersion")
        )

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
    }
}

tasks {
    patchPluginXml {
        sinceBuild.set("251")
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
