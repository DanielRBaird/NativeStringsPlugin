package com.danielrbaird.nativeStrings

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

open class GenerateImplementationsTask : DefaultTask() {

    private var destination: String? = null    // The main output folder
    private var input: String? = null          // The primary input file
    private var localesFile: String? = null    // The locales txt file
    private var jsonFolder: String? = null     // The folder containing all of the json for the strings

    fun setDestination(destination: String) {
        this.destination = destination
    }

    @OutputDirectory
    fun getDestination(): File {
        return project.file(destination!!)
    }

    fun setJsonFolder(jsonFolder: String) {
        this.jsonFolder = jsonFolder
    }

    @InputFiles
    fun getJsonFolder(): File {
        return project.file(jsonFolder!!)
    }

    fun setInput(input: String) {
        this.input = input
    }

    @InputFile
    fun getInput(): File {
        return project.file(input!!)
    }

    fun setLocales(localesFile: String) {
        this.localesFile = localesFile
    }

    @InputFile
    fun getLocales(): File {
        return project.file(localesFile!!)
    }

    @TaskAction
    fun execute() {
        // Get the list of locale files that already exist.
        val localeFiles = getLocaleFiles()

        getDestination().mkdir()

        val implFile = File(getDestination(), "StringsImpl.kt")
        implFile.writeText("")

        val stringBuilder = StringBuilder()

        // This gets the list of all the keys so that we can make sure we account for all of them with each of the languages.
        val allKeys = getSetOfIds(getInput())

        for (localeFile in localeFiles) {
            val unusedKeys = allKeys.toMutableSet()

            // Grab the json text out of the string file and get the array.
            val json: ArrayList<Map<String, Any>> = JsonSlurper().parseText(localeFile.file.readText()) as ArrayList<Map<String, Any>>

            stringBuilder.appendln("internal class ${localeFile.name}Strings : Strings {")

            for (stringObject in json) {
                val id = stringObject["id"]
                val translation = stringObject["translation"]
                unusedKeys.remove(id)
                stringBuilder.appendln("    override val $id: String by lazy { \"$translation\" }")
            }

            // If there are strings in the default file that don't exist in this one, we don't want to break things by not
            // having the implementation complete, so we just need to have an empty string.
            for (unusedKey in unusedKeys) {
                stringBuilder.appendln("    override val ${unusedKey}: String by lazy { \"\" }")
            }

            stringBuilder.appendln("}")
        }

        implFile.writeText(stringBuilder.toString())
    }

    // Gets the list of file names and files that currently exist.
    private fun getLocaleFiles(): List<LocaleFile> {
        val localesFile = getLocales()

        // Get the list of locale files that already exist.
        val locales = localesFile.readLines()
        val localeFiles = mutableListOf<LocaleFile>()
        for (locale in locales) {
            val stringsJsonFile = File(getJsonFolder(), "strings${locale}.json")
            if (stringsJsonFile.exists()) {
                localeFiles.add(LocaleFile(file = stringsJsonFile, name = locale))
            }
        }

        return localeFiles
    }

    // This expects the file passed in to be in the standard strings json format that contains an "id" field.
    private fun getSetOfIds(file: File): HashSet<String> {
        val hashSet = HashSet<String>()
        val json: ArrayList<Map<String, Any>> = JsonSlurper().parseText(file.readText()) as ArrayList<Map<String, Any>>

        // This dictionary will contain a map of all of the ids, and the translation for the default language.
        for (stringObject in json) {
            hashSet.add(stringObject["id"] as String)
        }

        return hashSet
    }
}