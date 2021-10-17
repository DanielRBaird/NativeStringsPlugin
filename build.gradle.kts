group = "com.danielrbaird.nativeStrings"
version = "1.0.16-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.5.31"
    id("com.gradle.plugin-publish") version "0.16.0"
}

dependencies {
    implementation(kotlin("stdlib", "1.4.0"))
    testCompile(gradleTestKit())
    testCompile("junit:junit:4+")
}

// NativeStrings plugin configuration.

pluginBundle {
    website = "https://github.com/DanielRBaird/NativeStringsPlugin"
    vcsUrl = "https://github.com/DanielRBaird/NativeStringsPlugin"
    tags = listOf("localization", "kotlin", "kotlin native", "kotlin/native")
}

gradlePlugin {
    plugins {
        create("nativeStringsPlugin") {
            id = "com.danielrbaird.nativeStrings"
            displayName = "NativeStrings"
            description = "A simple plugin for generating strongly typed localized strings in Kotlin Native"
            implementationClass = "com.danielrbaird.nativeStrings.NativeStrings"
        }
    }
}
