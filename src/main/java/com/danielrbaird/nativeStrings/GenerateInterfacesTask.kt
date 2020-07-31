package com.danielrbaird.nativeStrings

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateInterfacesTask : DefaultTask() {
    private var destination: Any? = null
    private var input: Any? = null

    fun setDestination(destination: String) {
        this.destination = destination
    }

    private fun getDestination(): File {
        return project.file(destination!!)
    }

    fun setInput(input: String) {
        this.input = input
    }

    @InputFile
    fun getInput(): File {
        return project.file(input!!)
    }

    @TaskAction
    fun execute() {
        // Make sure that we have the output path, and it has been created.
        val outputDir = getDestination()
        outputDir.mkdirs()

        // This should already exist.
        val input = getInput()

        val outputFile = File(outputDir, "Strings.kt")
        val stringBuilder = kotlin.text.StringBuilder()

        // Grab the json text out of the strings file, and get the array.
        val json: ArrayList<Map<String, Any>> = JsonSlurper().parseText(input.readText()) as ArrayList<Map<String, Any>>

        stringBuilder.appendln("internal interface Strings {")

        // This dictionary will contain a map of all of the ids, and the translation for the default language.
        for (stringObject in json) {
            val id = stringObject["id"] as String
            stringBuilder.appendln("    val $id: String")
        }

        stringBuilder.appendln("}")

        outputFile.writeText(stringBuilder.toString())
    }
}