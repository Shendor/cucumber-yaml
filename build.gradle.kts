import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

apply(plugin = "org.jetbrains.intellij.platform")

kotlin {
    jvmToolchain(27)
}

// Build with JDK 27 while retaining compatibility with the IDE's bundled runtime.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.withType<KotlinCompile>().configureEach {
    // The IntelliJ manifest task reads the target directly from compile tasks.
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

intellijPlatform {
    pluginConfiguration {
        name.set("Chronos")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    implementation("io.cucumber:cucumber-java:7.2.3")

    intellijPlatform {
        testFramework(TestFrameworkType.Platform)
        create(
            type = providers.gradleProperty("platformType"),
            version = providers.gradleProperty("platformVersion")
        )

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
    }
}

tasks {
    test {
        systemProperty("idea.load.plugins.id", "org.chronos")
    }

    patchPluginXml {
        sinceBuild.set("262")
        // untilBuild.set("")
        pluginDescription.set(
            """
              <p>
                This plugin enables <a href="https://cucumber.io/">Cucumber</a> support with step definitions written in Yaml (aka Chronos test definitions).
              </p>
              <p>
                The following coding assistance features are available:
              </p>
              <ul>
                <li>Navigation from Cucumber feature file to YAML.
                <li>Navigation from Java step def to YAML.
                <li>Navigation from YAML to Cucumber.
                <li>References from property key to property declarations in .properties or .yaml files.
                <li>References from ftl file path to a file.
                <li>Documentation in tooltips.
                <li>Keywords autocomplete.
                <li>Customisable colour scheme for Chronos syntax.
              </ul>
        """
        )
        changeNotes.set(
            """
      <ul>
        <li><b>1.0.0</b> <em>(2023-01-04)</em> - Initial release</li>
        <li><b>1.0.0</b> <em>(2025-20-01)</em> - New YML-Cucumber and UI Elements references</li>
        <li><b>1.1.0</b> <em>(2026-07-01)</em> - Added support of references from property key to property declarations in .properties or .yaml files.
        Added support of references from ftl file path to file</li>
        <li><b>1.2.0</b> <em>(2026-12-01)</em> - Improved colour scheme and option to edit it</li>
        <li><b>Updates since 1.2.0</b> <em>(2026-10-28)</em>
          <ul>
            <li>Updated compatibility to IntelliJ IDEA 2026.2.3 (build 262).</li>
            <li>Added YAML function completion after <code>#</code> and navigation to Java methods annotated with <code>@ProcessingFunction</code>, including dependency methods.</li>
            <li>Fixed function selection inside nested expressions such as <code>ftl($('data/test-data-'+#get('id')+'.ftl'))</code>, so only <code>#get</code> is selected.</li>
            <li>Restricted Java Cucumber step navigation and completion to sources in the feature's module and dependent project modules, excluding external libraries and attached library sources.</li>
            <li>Added navigation from YAML <code>page</code> values to Java <code>@PageAccessor</code> classes and improved <code>element</code> references to <code>@Locator</code> declarations.</li>
            <li>Added Find Usages and gutter navigation from Java page and locator declarations to their YAML usages.</li>
            <li>Added bidirectional navigation between YAML <code>test</code>/<code>data-test</code> and matching <code>name</code> values, with navigation from test definitions to Java and Cucumber usages.</li>
            <li>Fixed YAML parent reference ownership and text ranges, and improved step usage searches to avoid recursive resolution and duplicate targets.</li>
            <li>Improved property placeholder handling for default values, avoiding false unresolved-property warnings.</li>
          </ul>
        </li>
      </ul>
    """
        )
    }
}
