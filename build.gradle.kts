import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.intellij") version "1.7.0"
}

group = "cm.vk"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = properties("javaVersion")
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    test {
        isScanForTestClasses = false
    }
}
