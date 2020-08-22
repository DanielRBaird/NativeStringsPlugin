package com.danielrbaird.nativeStrings

import groovy.json.JsonSlurper
import java.io.File
import java.lang.StringBuilder

/**
 * This just has a bunch of our basic logic and static naming for things that we don't want to have to rewrite in different places.
 */
internal object FileHelper {
    const val localesFileName = "locales.txt"
    const val implementationFileName = "StringsImpl.kt"
    const val interfaceFileName = "Strings.kt"
    const val enumFileName = "Locales.kt"

    // We always expect double delimiter characters.
    const val paramDelimiterLeft = '{'
    const val paramDelimiterRight = '}'

    const val idKey = "id"
    const val translationKey = "translation"

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

    // Gets the locale files that contain the json for the various locales.
    // Will also include files that have not yet been created. All the strings
    // will be treated as missing, and will use the default language strings.
    internal fun getLocaleFiles(directory: File, locales: List<String>): List<LocaleFile> {
        // Get the list of locale files that already exist.
        val localeFiles = mutableListOf<LocaleFile>()
        for (locale in locales) {
            val stringsJsonFile = getStringsFileForLocale(directory, locale)
            localeFiles.add(LocaleFile(file = stringsJsonFile, name = locale))
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

    /**
     * Looks in the translation string for param ranges by finding things would double
     * delimiters on each side, and gives the range of the entire delimiter area.
     * This can be used to extract the variables names, or mutate the string for use in kotlin.
     */
    internal fun findParamRanges(translation: String): List<IntRange> {
        val params = mutableListOf<IntRange>()
        var lastChar: Char? = null
        var readingParam = false
        var paramStart = 0
        translation.forEachIndexed { index, c ->
            // If we find two in a row then we are looking at a param.
            if (c == paramDelimiterLeft && lastChar == paramDelimiterLeft) {
                readingParam = true
                paramStart = index - 1
            }

            if (readingParam && c == paramDelimiterRight && lastChar == paramDelimiterRight) {
                readingParam = false
                params.add(IntRange(paramStart, index))
            }

            lastChar = c
        }

        return params
    }

    /**
     * Extracts the names of parameters used in a translation string.
     */
    internal fun findParamNames(translation: String): List<String> {
        return findParamNames(translation, findParamRanges(translation))
    }

    internal fun findParamNames(translation: String, paramRanges: List<IntRange>): List<String> {
        return paramRanges.map {
            // Adding 3 because we are skipping the two delimiters and the '.'
            // subtracting two because we are removing the trailing delimiters.
            translation.substring(IntRange(it.first + 3, it.last - 2))
        }
    }

    /**
     * Generates the method name for a parameterized string. Example:
     * id: "string_with_param"
     * translation: "translated string with number: {{.Number}}
     *
     * fun string_with_param(Number: String): String
     *
     * Note: Currently only string parameters are supported.
     */
    internal fun generateParameterizedStringMethodName(id: String, paramNames: List<String>): String {
        val stringBuilder = StringBuilder()

        // We need a method rather than just a val
        stringBuilder.append("fun $id(")
        paramNames.forEachIndexed { index, parameter ->
            stringBuilder.append("$parameter: String")
            if (index != paramNames.lastIndex) {
                stringBuilder.append(", ")
            } else {
                stringBuilder.append("): String")
            }
        }

        return stringBuilder.toString()
    }

    /**
     * This used to take a string that looks like this:
     * "translated string with a number: {{.Number}}"
     * and make it look like this
     * "translated string with a number: $Number"
     *
     * We could get rid of some of these params since we really only need the translation
     * but at this point the caller should already have that data available.
     */
    @ExperimentalStdlibApi
    internal fun replaceParameterRangesWithKotlinParams(
            translation: String,
            paramRanges: List<IntRange>,
            paramNames: List<String>): String {
        // We are going to go through the ranges backwards so that we can modify the string and still have
        // valid ranges earlier in the string
        val ranges = paramRanges.reversed()
        val params = paramNames.reversed().toMutableList()
        var newTranslation = translation

        for (range in ranges) {
            newTranslation = newTranslation.replace(translation.substring(range), "$" + params.removeFirst())
        }

        return newTranslation
    }
}