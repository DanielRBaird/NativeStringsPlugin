# NativeStringsPlugin

The goal of this project is to provide a simple way to add localized strings resources into your Kotlin Native project, and be able to access them as hard coded strings.

I also wanted to avoid the pitfalls of a kvp solution where we would be required to either read from a file when accessing strings, or load the strings into memory when we launch the app.

This solution should allow you all of the benefits of being able to use hard coded resources in your app, while also keeping the overhead very low because all of the strings are lazy loaded, and are not fetched from disk.

## Usage
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
  }
]
```
Right now this is the only supported format, where each entry has an `id` and a `translation`. In code, `id` becomes the name of the property that you access.

Once you have defined your files, you can apply the plugin like so:
```kotlin
apply<NativeStrings>()

// Configure the extension using a DSL block
configure<NativeStringsPluginExtension> {
    input = "test/stringsDefault.json"
    localesFile = "test/locales.txt"
    destination = "someGeneratedSourceFolder"
    jsonFolder = "folderWhereIKeepMyJson"
}
```

Ultimately, you will need to generate the other laugnage files somehow. A tool like PhraseApp can handle taking your default json file and giving you the translations.

Theses files should just have names that match `strings{languageCode}.json` with the language codes available in your locales file.

To run the code generator, just call `generate`.

This will create two files.

`Strings.kt` interface, which will have an item for every key that exists in your default strings json.

and 

`StringsImpl.kt` which will contain an implementation for every language that you support.
