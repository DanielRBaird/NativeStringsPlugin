package com.danielrbaird.nativeStrings

import org.gradle.api.Plugin
import org.gradle.api.Project

class NativeStrings : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("nativeStringsPlugin", NativeStringsPluginExtension::class.java)

        project.tasks.register("nativeStringsGenerateInterface", GenerateInterfacesTask::class.java) { task ->
            task.setInputFolder(extension.inputFolder)
            task.setDestinationFolder(extension.destinationFolder)
            task.packageName = extension.packageName
        }

        project.tasks.register("nativeStringsGenerateImplementation", GenerateImplementationsTask::class.java) { task ->
            task.setInputFolder(extension.inputFolder)
            task.setDestinationFolder(extension.destinationFolder)
            task.packageName = extension.packageName
        }

        project.tasks.register("nativeStringsGenerateEnum", GenerateEnumTask::class.java) { task ->
            task.setInputFolder(extension.inputFolder)
            task.setDestinationFolder(extension.destinationFolder)
            task.packageName = extension.packageName
        }

        project.tasks.register("nativeStringsGenerateAll") { task ->
            task.dependsOn("nativeStringsGenerateInterface")
            task.dependsOn("nativeStringsGenerateImplementation")
            task.dependsOn("nativeStringsGenerateEnum")
        }
    }
}