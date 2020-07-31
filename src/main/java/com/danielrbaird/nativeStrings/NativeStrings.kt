package com.danielrbaird.nativeStrings

import org.gradle.api.Plugin
import org.gradle.api.Project

class NativeStrings : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("nativeStringsPlugin", NativeStringsPluginExtension::class.java)

        project.tasks.register("generateInterface", GenerateInterfacesTask::class.java) { task ->
            task.setInput(extension.input)
            task.setDestination(extension.destination)
        }

        project.tasks.register("generateImplementation", GenerateImplementationsTask::class.java) { task ->
            task.dependsOn("generateInterface")
            task.setInput(extension.input)
            task.setDestination(extension.destination)
            task.setLocales(extension.localesFile)
            task.setJsonFolder(extension.jsonFolder)
        }
    }
}