plugins {
  kotlin("jvm") version "2.3.10"

  `maven-publish`
  id("com.ncorti.ktfmt.gradle") version "0.26.0"

  id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
  kotlin("plugin.allopen") version "2.3.10"
}

group = "me.ppvrflw"

version = "0.1.0"

repositories { mavenCentral() }

dependencies {
  testImplementation(kotlin("test"))
  testImplementation("io.kotest:kotest-runner-junit5-jvm:6.1.10")
  runtimeOnly("io.kotest:kotest-assertions-core:6.1.10")

  testImplementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.14")
}

kotlin { jvmToolchain(24) }

tasks.test { useJUnitPlatform() }

allOpen { annotation("org.openjdk.jmh.annotations.State") }

benchmark {
  targets { register("test") }

  configurations {
    named("main") {
      includes = mutableListOf("me.ppvrflw.benchmark.*")
      warmups = 10
      iterations = 10
      iterationTime = 1
      iterationTimeUnit = "s"
    }
  }
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/ppvrflow/flint")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }
  publications { register<MavenPublication>("gpr") { from(components["java"]) } }
}
