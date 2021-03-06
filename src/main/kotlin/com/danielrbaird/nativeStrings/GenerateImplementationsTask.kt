package com.danielrbaird.nativeStrings

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

/**
 * Generates the implementations for the strings interface.
 * Expects there to be a locales file called "locales.txt"
 * Output will be named "StringsImpl.kt"
 */
open class GenerateImplementationsTask : DefaultTask() {
    private var destinationFolder: String? = null    // The main output folder
    private var inputFolder: String? = null          // The folder containing all of the json for the strings

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

    @ExperimentalStdlibApi
    @TaskAction
    fun execute() {
        val inputFolder = getInputFolder()
        val outputFolder = getDestinationFolder()
        outputFolder.mkdir()

        // Get the list of locale files that already exist.
        val locales = FileHelper.getLocales(File(inputFolder, FileHelper.localesFileName))
        val localeFiles = FileHelper.getLocaleFiles(inputFolder, locales)

        val implFile = FileHelper.getImplementationFile(outputFolder)
        implFile.writeText("")

        val stringBuilder = StringBuilder()

        if (localeFiles.isEmpty()) {
            throw Exception("Locales file did not exist, or did not contain any languages.")
        }

        // We expect the first language listed to be the "default", meaning the one that will be edited by developers
        // and contain all of the keys that they are using locally.
        val defaultLanguageFile = localeFiles.first().file

        // This gets the list of all the keys so that we can make sure we account for all of them with each of the languages.
        val allKeys = FileHelper.getSetOfIds(defaultLanguageFile)

        stringBuilder.appendln("package $packageName\n")


        val defaultTranslationJson: ArrayList<Map<String, Any>> = JsonSlurper().parseText(defaultLanguageFile.readText()) as ArrayList<Map<String, Any>>

        for (localeFile in localeFiles) {
            val unusedKeys = allKeys.toMutableSet()

            // Grab the json text out of the string file and get the array.
            val json: ArrayList<Map<String, Any>> = if (localeFile.file.exists()) {
                JsonSlurper().parseText(localeFile.file.readText()) as ArrayList<Map<String, Any>>
            } else {
                ArrayList()
            }

            stringBuilder.appendln("internal class ${localeFile.name}Strings : Strings {")

            for (stringObject in json) {
                val id = stringObject[FileHelper.idKey] as String
                val translation = stringObject[FileHelper.translationKey] as String
                unusedKeys.remove(id)
                addMethodOrPropertyFor(id, translation, stringBuilder)
            }

            // If there are strings in the default file that don't exist in this one, we don't want to break things by not
            // having the implementation complete, so we just need to use the default translation.
            for (unusedKey in unusedKeys) {
                // Use the default language translation to pull out the params.
                val defaultLanguageObject = defaultTranslationJson.find { it[FileHelper.idKey] as String == unusedKey }!!
                val translation = defaultLanguageObject[FileHelper.translationKey] as String
                addMethodOrPropertyFor(unusedKey, translation, stringBuilder)
            }

            stringBuilder.appendln("}")
            stringBuilder.appendln()
        }

        implFile.writeText(stringBuilder.toString())
    }

    @ExperimentalStdlibApi
    fun addMethodOrPropertyFor(id: String, translation: String, stringBuilder: StringBuilder) {
        val paramRanges = FileHelper.findParamRanges(translation)

        if (paramRanges.isEmpty()) {
            // If we have no parameters we are using a lazy val.
            stringBuilder.appendln("    override val $id: String by lazy { \"$translation\" }")
        } else {
            val paramNames = FileHelper.findParamNames(translation, paramRanges)
            // In the case that we have parameters we are generating the implementation of a method.
            stringBuilder.appendln("    override ${FileHelper.generateParameterizedStringMethodName(id, paramNames)} {")
            stringBuilder.appendln("        return \"${FileHelper.replaceParameterRangesWithKotlinParams(translation, paramRanges, paramNames)}\"")
            stringBuilder.appendln("    }")
        }
    }
}