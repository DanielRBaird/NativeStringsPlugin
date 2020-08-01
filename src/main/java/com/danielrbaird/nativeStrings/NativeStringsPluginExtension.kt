package com.danielrbaird.nativeStrings

open class NativeStringsPluginExtension {
    /**
     * The folder path where we are ultimately going to store all of the output from the code generator
     */
    var destinationFolder: String = ""

    /**
     * The folder that contains your json string files, and your locales list.
     */
    var inputFolder: String = ""

    /**
     * Package name that we want to add to the files that we generate.
     */
    var packageName: String = ""
}