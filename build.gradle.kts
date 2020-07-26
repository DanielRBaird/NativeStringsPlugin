import groovy.json.JsonSlurper

group = "org.example"
version = "1.0-SNAPSHOT"

data class LocaleFile(
    var file: File,
    var name: String
)

open class NativeStringsPluginExtension {
    var message: String? = null
    var greeter: String? = null
}

open class GenerateInterfacesTask : DefaultTask() {
    val keySet: HashSet<String> = HashSet()

    var destination: Any? = null
    var input: Any? = null

    fun getDestination(): File {
        return project.file(destination!!)
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

        stringBuilder.appendln(
            """
            internal interface Strings {
            """.trimIndent()
        )

        // This dictionary will contain a map of all of the ids, and the translation for the default language.
        for (stringObject in json) {
            val id = stringObject["id"] as String
            stringBuilder.appendln("""    val $id: String""")
            keySet.add(id)
        }

        stringBuilder.appendln("""}""")

        outputFile.writeText(stringBuilder.toString())
    }
}

tasks.register<GenerateInterfacesTask>("generateInterface") {
    input = { project.extra["localizableStringsFile"]!! }
    destination = { project.extra["localizableStringsOutputPath"]!! }
}

tasks.register("generate") {
    dependsOn("generateInterface")
}

// Testing
// extra["localizableStringsFile"] = "test/stringsDefault.json"
// extra["localizableStringsOutputPath"] = "test"

// TODO: How does the plugin project work with this?
class NativeStrings : Plugin<Project> {
    override fun apply(project: Project) {
        //val extension = project.extensions.create<GreetingPluginExtension>("nativeStringsPlugin")
        project.task("generateStrings") {
            dependsOn.add("generateInterface")
        }
    }
}
