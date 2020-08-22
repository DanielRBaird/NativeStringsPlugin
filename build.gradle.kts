group = "com.danielrbaird.nativeStrings"
version = "1.0.16-SNAPSHOT"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.4.0"
    id("com.gradle.plugin-publish") version "0.12.0"
}

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

dependencies {
    implementation(kotlin("stdlib", "1.4.0"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}