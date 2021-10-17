package com.danielrbaird.nativeStrings

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.TaskOutcome.*
import java.io.File

class PluginTest {
    private val inputFolderName = "input"
    private val outputFolderName = "output"

    private lateinit var testProjectDir: TemporaryFolder

    private lateinit var buildGradle: File

    @Before
    fun setup() {
        testProjectDir = TemporaryFolder()
        testProjectDir.create()
        // Prepare build.gradle
        buildGradle = testProjectDir.newFile("build.gradle.kts")
        val fileText = """
            import com.danielrbaird.nativeStrings.NativeStrings
            import com.danielrbaird.nativeStrings.NativeStringsPluginExtension
            
            plugins {
                id("com.danielrbaird.nativeStrings")
            }
            
            apply<NativeStrings>()
            configure<NativeStringsPluginExtension> {
                destinationFolder = "$outputFolderName"
                inputFolder = "$inputFolderName"
                packageName = "com.daniel.test"
            }""".trimIndent()
        buildGradle.writeText(fileText)
    }

    /**
     * Helper method that runs a Gradle task in the testProjectDir
     * @param arguments the task arguments to execute
     * @param isSuccessExpected boolean representing whether or not the build is supposed to fail
     * @return the task's BuildResult
     */
    private fun gradle(isSuccessExpected: Boolean = true, arguments: MutableList<String> = mutableListOf("tasks")): BuildResult {
        arguments += "--stacktrace"
        val runner = GradleRunner.create()
                .withArguments(arguments)
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withDebug(true)
        return if (isSuccessExpected) runner.build() else runner.buildAndFail()
    }

    @Test
    fun `locales enum is generated correctly`() {
        // We need to actually create the input folder and set the proper files in it.
        val testLocalesText = "En\nFr"
        val stringsEnJsonText = """
            [
              {
                "id": "test_string",
                "translation": "test string"
              },
              {
                "id": "param_test",
                "translation": "param test {{.param}}!"
              }
            ]""".trimIndent()

        val inputFolder = testProjectDir.newFolder(inputFolderName)
        val outputFolder = testProjectDir.newFolder(outputFolderName)

        val testLocalesFile = File(inputFolder, FileHelper.localesFileName)
        testLocalesFile.writeText(testLocalesText)

        val testJsonFile = FileHelper.getDefaultStringsFile(inputFolder)
        testJsonFile.writeText(stringsEnJsonText)

        // Run the task to generate the locales enum.
        val result = gradle(arguments = mutableListOf("nativeStringsGenerateEnum"))
        assert(result.task(":nativeStringsGenerateEnum")?.outcome == SUCCESS)

        val expectedOutputText = """
        package com.daniel.test

        internal enum class Locale {
            En,
            Fr;

            companion object {
                fun localizedStrings(locale: Locale): Strings {
                    return when (locale) {
                        Locale.En -> EnStrings()
                        Locale.Fr -> FrStrings()
                    }
                }
            }
        }
        """.trimIndent()

        val enumFile = File(outputFolder, FileHelper.enumFileName)
        assert(enumFile.exists()) { "Enum file was not created" }
        assert(enumFile.readText() == expectedOutputText) { enumFile.readText() }
    }

    @Test
    fun `strings interface is generated correctly`() {
        // We need to actually create the input folder and set the proper files in it.
        val testLocalesText = "En\nFr"
        val stringsEnJsonText = """
            [
              {
                "id": "test_string",
                "translation": "test string"
              },
              {
                "id": "param_test",
                "translation": "param test {{.param}}!"
              }
            ]""".trimIndent()

        val inputFolder = testProjectDir.newFolder(inputFolderName)
        val outputFolder = testProjectDir.newFolder(outputFolderName)

        val testLocalesFile = File(inputFolder, FileHelper.localesFileName)
        testLocalesFile.writeText(testLocalesText)

        val testJsonFile = FileHelper.getDefaultStringsFile(inputFolder)
        testJsonFile.writeText(stringsEnJsonText)

        // Run the task to generate the locales enum.
        val result = gradle(arguments = mutableListOf("nativeStringsGenerateInterface"))
        assert(result.task(":nativeStringsGenerateInterface")?.outcome == SUCCESS)

        val expectedOutputText = """
        package com.daniel.test

        internal interface Strings {
            val test_string: String
            fun param_test(param: String): String
        }
        """.trimIndent()

        val interfaceFile = File(outputFolder, FileHelper.interfaceFileName)
        assert(interfaceFile.exists()) { "Interface file was not created" }
        assert(interfaceFile.readText() == expectedOutputText) { interfaceFile.readText() }
    }
}