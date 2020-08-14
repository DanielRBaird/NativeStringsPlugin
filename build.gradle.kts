import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.danielrbaird.nativeStrings"
version = "1.0.11-SNAPSHOT"

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.0-rc"
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
    implementation(kotlin("stdlib-jdk8"))
}

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}