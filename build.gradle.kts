import io.papermc.hangarpublishplugin.model.Platforms
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  java
  id("com.gradleup.shadow") version "9.0.0-beta4"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
val versionProvider = fetchCommitsSinceMidnight().map { "${dateFormatter.format(LocalDate.now())}-$it" }

group = "io.github.emilyy-dev"
version = versionProvider.get()

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

tasks {
  processResources {
    inputs.property("version", versionProvider)
    doFirst {
      expand(mapOf("version" to versionProvider.get()))
    }
  }

  assemble {
    dependsOn(shadowJar)
  }

  jar {
    archiveClassifier = "noshade"
    archiveVersion = versionProvider
    metaInf.from("COPYING")
  }

  shadowJar {
    archiveClassifier = null
    archiveVersion = versionProvider
    mergeServiceFiles()
    metaInf.from("COPYING")

    relocate("com.fasterxml.jackson", "ar.emily.adorena.libs.jackson")
    relocate("org.yaml.snakeyaml", "ar.emily.adorena.libs.snakeyaml")

    relocate("com.github.benmanes.caffeine", "ar.emily.adorena.libs.caffeine")
    relocate("org.checkerframework", "ar.emily.adorena.libs.checkerqual")
    relocate("com.google.errorprone.annotations", "ar.emily.adorena.libs.errorprone")

    exclude("META-INF/maven/**", "**/module-info.class", "**/*NOTICE*", "**/*LICENSE*")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
      attributes["paperweight-mappings-namespace"] = "mojang"
      attributes["Multi-Release"] = "true"
    }
  }

  runServer {
    minecraftVersion("1.21.4")
  }

  val publishToPluginRepos by registering {
    dependsOn(publishAllPublicationsToHangar)
  }
}

val supportedMinecraftVersions = listOf("1.21.3", "1.21.4")

hangarPublish {
  publications.register("adorena") {
    id = "adorena"
    version = versionProvider
    channel = "Release"
    changelog = fetchLastCommitMessage()
    apiKey = providers.environmentVariable("HANGAR_API_KEY")
    platforms {
      register(Platforms.PAPER) {
        jar = tasks.shadowJar.flatMap { it.archiveFile }
        platformVersions = supportedMinecraftVersions
      }
    }
  }
}

fun fetchCommitsSinceMidnight(): Provider<String> =
  providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD", "--since=midnight", "--grep=^\\[ci skip\\]", "--invert-grep")
  }.standardOutput.asText.map(String::trim)

fun fetchLastCommitMessage(): Provider<String> =
  providers.exec { commandLine("git", "log", "-1", "--pretty=%B") }.standardOutput.asText.map(String::trim)
