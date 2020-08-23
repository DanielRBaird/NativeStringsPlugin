package com.danielrbaird.nativeStrings

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

open class GenerateInterfacesTask : DefaultTask() {
    private var destinationFolder: String? = null
    private var inputFolder: String? = null

    @Input
    var packageName: String? = null

    fun setDestinationFolder(destinationFolder: String) {
        this.destinationFolder = destinationFolder
    }

    @OutputDirectory
    fun getDestinationFolder(): File {
        return project.file(destinationFolder!!)
    }

    fun setInputFolder(inputFolder: String) {
        this.inputFolder = inputFolder
    }

    @InputDirectory
    fun getInputFolder(): File {
        return project.file(inputFolder!!)
    }

    @TaskAction
    fun execute() {
        // Make sure that we have the output path, and it has been created.
        val outputDir = getDestinationFolder()
        outputDir.mkdirs()

        // This should already exist.
        val inputFolder = getInputFolder()
        val defaultStringsFile = FileHelper.getDefaultStringsFile(inputFolder)

        val outputFile = File(outputDir, "Strings.kt")
        val stringBuilder = kotlin.text.StringBuilder()

        // Grab the json text out of the strings file, and get the array.
        val json: ArrayList<Map<String, Any>> = JsonSlurper().parseText(defaultStringsFile.readText()) as ArrayList<Map<String, Any>>

        stringBuilder.appendln("package $packageName\n")
        stringBuilder.appendln("internal interface Strings {")

        // This dictionary will contain a map of all of the ids, and the translation for the default language.
        for (stringObject in json) {
            // TODO: At this point we don't handle pluralization, so it is expected that the translation is just a string.
            // this is something we need to support eventually.

            val id = stringObject[FileHelper.idKey] as String
            val translation = stringObject[FileHelper.translationKey] as String
            val paramNames = FileHelper.findParamNames(translation)

            if (paramNames.isEmpty()) {
                stringBuilder.appendln("    val $id: String")
            } else {
                // We need a method rather than just a val
                stringBuilder.appendln("    ${FileHelper.generateParameterizedStringMethodName(id, paramNames)}")
            }
        }

        stringBuilder.append("}")

        outputFile.writeText(stringBuilder.toString())
    }
}