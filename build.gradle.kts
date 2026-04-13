plugins {
  kotlin("jvm") version "2.3.20"

  // Publishing
  `maven-publish`

  // Formatting
  id("com.ncorti.ktfmt.gradle") version "0.26.0"

  // Benchmarking
  id("org.jetbrains.kotlinx.benchmark") version "0.4.16"
  kotlin("plugin.allopen") version "2.3.20"

  // Testing
  id("io.kotest").version("6.1.11")
  id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

group = "me.ppvrflw"

version =
    providers
        .environmentVariable("VERSION")
        .map { it.removePrefix("v") }
        .getOrElse("0.1.0-SNAPSHOT")

repositories { mavenCentral() }

dependencies {
  testImplementation(kotlin("test"))

  testImplementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.16")

  testImplementation("io.kotest:kotest-runner-junit5-jvm:6.1.11")
  testRuntimeOnly("io.kotest:kotest-assertions-core:6.1.11")
}

kotlin { jvmToolchain(25) }

// Benchmarking
allOpen { annotation("org.openjdk.jmh.annotations.State") }

benchmark {
  targets { register("test") }

  configurations {
    named("main") {
      includes = mutableListOf("me.ppvrflw.benchmark.*")
      warmups = 5
      iterations = 10
      iterationTime = 1
      iterationTimeUnit = "s"
      reportFormat = "json"
    }
  }
}

// Publishing
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/ppvrflw/flint")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }
  publications { register<MavenPublication>("gpr") { from(components["java"]) } }
}

// Testing
tasks.withType<Test>().configureEach { useJUnitPlatform() }

kover {
  reports {
    verify {
      rule {
        minBound(80) // fail if coverage < 80%
      }
    }
  }
}
