import groovy.json.JsonSlurper

group = "org.example"
version = "1.0-SNAPSHOT"

data class LocaleFile(
    var file: File,
    var name: String
)

open class GenerateInterfacesTask : DefaultTask() {
    val keySet: HashSet<String> = HashSet()

    var destination: Any? = null
    var input: Any? = null

    private fun getDestination(): File {
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

        stringBuilder.appendln("""internal interface Strings {""")

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

/*tasks.register<GenerateInterfacesTask>("generateInterface") {
    input = { project.extra["localizableStringsFile"]!! }
    destination = { project.extra["localizableStringsOutputPath"]!! }
}*/

// This works to be able to run the generate thing.
/*tasks.register("generate") {
    dependsOn("generateInterface")
}*/

// Testing
// extra["localizableStringsFile"] = "test/stringsDefault.json"
// extra["localizableStringsOutputPath"] = "test"

open class NativeStringsPluginExtension {
    var destination: String? = null
    var input: String? = null
}

class NativeStrings : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<NativeStringsPluginExtension>("nativeStringsPlugin")
        project.tasks.register<GenerateInterfacesTask>("generateInterface") {
            input = extension.input
            destination = extension.destination
        }
    }
}

// Note: I think this is what would have to be done in another project:

apply<NativeStrings>()

// Configure the extension using a DSL block
configure<NativeStringsPluginExtension> {
    input = "test/stringsDefault.json"
    destination = "test"
}