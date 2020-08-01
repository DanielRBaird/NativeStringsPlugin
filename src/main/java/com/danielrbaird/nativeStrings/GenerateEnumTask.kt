package com.danielrbaird.nativeStrings

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

/**
 * This will generate the enum of the different locale options available.
 */
open class GenerateEnumTask : DefaultTask() {
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

        val localesFile = FileHelper.getLocalesFile(getInputFolder())
        val locales = FileHelper.getLocales(localesFile)

        val outputFile = File(outputDir, FileHelper.enumFileName)
        val stringBuilder = kotlin.text.StringBuilder()

        stringBuilder.appendln("package $packageName\n")
        stringBuilder.appendln("internal enum class Locale {")

        for (locale in locales) {
            stringBuilder.append("    $locale,")
        }
        stringBuilder.removeSuffix(",")
        stringBuilder.append("}")
        outputFile.writeText(stringBuilder.toString())
    }
}