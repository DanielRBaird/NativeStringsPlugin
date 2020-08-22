# NativeStringsPlugin

The goal of this project is to provide a simple way to add localized strings resources into your Kotlin Native project, and be able to access them as hard coded strings.

I also wanted to avoid the pitfalls of a kvp solution where we would be required to either read from a file when accessing strings, or load the strings into memory when we launch the app.

This solution should allow you all of the benefits of being able to use hard coded resources in your app, while also keeping the overhead very low because all of the strings are lazy loaded, and are not fetched from disk.

## Integration
This should be fairly straightforward to begin using.
You must have two different files. A Locales file, and a json file with your strings.

The locales file is a simple txt file with a language code/name on each line, example:
```
Default
Fr
Gr
```
I just named my english file `Default` because it is where I will be entering my strings that need to be translated. All of the other files will be created based on this one. 

You wil also need to create your primary strings file. Example:
```json
[
  {
    "id": "example_key",
    "translation": "example string"
  },
  {
    "id": "example_param_key",
    "translation": "example string with param {{.someParam}}"
  }
]
```
Right now this is the only supported format, where each entry has an `id` and a `translation`. In code, `id` becomes the name of the property that you access.

It is following this format here: https://help.phrase.com/help/go-i18n-json, but it lacks full support for the spec.

Once you have defined your files, you can apply the plugin like so:
```kotlin

// Pull in the dependency
plugins {
  id("com.danielrbaird.nativeStrings") version "1.0.12-SNAPSHOT"
}

// Apply the plugin
apply<com.danielrbaird.nativeStrings.NativeStrings>()

// Configure the plugin
configure<com.danielrbaird.nativeStrings.NativeStringsPluginExtension> {
    destinationFolder = "someOutputFolder" /* The folder where you want to output files */
    inputFolder = "someInputFolder"        /* The folder that contains your strings files */
    packageName = "com.company.myApp"      /* The package that your files will be in */
}
```

Ultimately, you will need to generate the other language files somehow. A tool like PhraseApp can handle taking your default json file and giving you the translations.

Theses files should just have names that match `strings{languageCode}.json` with the language codes available in your locales file.

To run the code generator, just call `nativeStringsGenerateAll`.

This will create three files.

`Strings.kt` interface, which will have an item for every key that exists in your default strings json.

`StringsImpl.kt` which will contain an implementation for every language that you support.

`Locales.kt` which contains an enum with an entry for each supported language. It also contains a method that maps from enum to implementation.

In order to access these things within your code, you will need to add it to your source dir just like any other generated files.

```
kotlin.sourceSets.named("commonMain") {
    this.kotlin.srcDir("someOutputFolder")
}
```

## Usage

Once you have the plugin setup, you just need to access the correct localized language and begin using the strings from that language.

Generally that flow should look something like this:

- Require the client application to provide a `Locale`.
- Use the method on the `Locale` companion to get the `Strings` implementation. ```val strings = Locale.localizedStrings(locale)```.
- Cache the resulting implementation if you want to keep lazy strings instantiated.
- Always use the `Strings` interface rather than a particular implementation.

## Future work

This is pretty bare bones right now, and I hope to improve it as I have time. Here is the list of things i'm currently planning to try to support:

- Simple formatted strings that take another string or number as input.
- Different formats for the strings json file.

