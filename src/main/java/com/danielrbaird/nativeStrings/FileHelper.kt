package com.danielrbaird.nativeStrings

import groovy.json.JsonSlurper
import java.io.File

/**
 * This just has a bunch of our basic logic and static naming for things that we don't want to have to rewrite in different places.
 */
internal object FileHelper {
    const val localesFileName = "locales.txt"
    const val implementationFileName = "StringsImpl.kt"
    const val interfaceFileName = "Strings.kt"
    const val enumFileName = "Locales.kt"

    internal fun getLocalesFile(directory: File): File {
        return File(directory, localesFileName)
    }

    internal fun getStringsFileForLocale(directory: File, locale: String): File {
        return File(directory, "strings${locale}.json")
    }

    internal fun getInterfaceFile(outputDirectory: File): File {
        return File(outputDirectory, interfaceFileName)
    }

    internal fun getImplementationFile(outputDirectory: File): File {
        return File(outputDirectory, implementationFileName)
    }

    /**
     * This just returns all of the lines from the locales file
     */
    internal fun getLocales(localesFile: File): List<String> {
        return localesFile.readLines()
    }

    internal fun getDefaultStringsFile(directory: File): File {
        val localesFile = getLocalesFile(directory)
        val defaultLocale = localesFile.readLines().first().trim()
        return getStringsFileForLocale(directory, defaultLocale)
    }

    // Gets the list of file names and files that currently exist.
    internal fun getLocaleFiles(directory: File, locales: List<String>): List<LocaleFile> {
        // Get the list of locale files that already exist.
        val localeFiles = mutableListOf<LocaleFile>()
        for (locale in locales) {
            val stringsJsonFile = getStringsFileForLocale(directory, locale)
            if (stringsJsonFile.exists()) {
                localeFiles.add(LocaleFile(file = stringsJsonFile, name = locale))
            }
        }

        return localeFiles
    }

    /**
     *  This expects the file passed in to be in the standard strings json format.
     *  Meaning it is an array of objects containing an "id" and a "translation".
     */
    internal fun getSetOfIds(file: File): HashSet<String> {
        val hashSet = HashSet<String>()
        val json: ArrayList<Map<String, Any>> = JsonSlurper().parseText(file.readText()) as ArrayList<Map<String, Any>>

        // This dictionary will contain a map of all of the ids, and the translation for the default language.
        for (stringObject in json) {
            hashSet.add(stringObject["id"] as String)
        }

        return hashSet
    }
}