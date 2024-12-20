import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
  java
  idea
  id("com.gradleup.shadow") version "8.3.4"
}

group = "io.github.emilyy-dev"
version = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

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
    inputs.property("version", version)
    expand(mapOf("version" to version))
  }

  assemble {
    dependsOn(shadowJar)
  }

  jar {
    archiveClassifier = "noshade"
    metaInf.from("COPYING")
  }

  shadowJar {
    archiveClassifier = null
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
}
