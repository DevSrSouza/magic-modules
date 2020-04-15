package io.labs.dotanuki.magicmodules.writer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

internal class ModuleNamesWriter {

    fun write(folder: File, filename: String, names: List<String>) {

        if (!folder.isDirectory) throw IllegalArgumentException(NOT_ACCEPTED_INPUT_FILE)

        if (names.isEmpty()) throw IllegalArgumentException(NOT_ACCEPTED_LIST_OF_NAMES)

        generateAndWriteKotlinCode(filename, names, folder)
    }

    private fun generateAndWriteKotlinCode(
        filename: String,
        names: List<String>,
        target: File
    ) {
        val objectWithConstantsSpec = TypeSpec.objectBuilder(filename)
            .apply { names.asConstantsPropertiesSpec().forEach { addProperty(it) } }
            .addProperty(names.asListPropertySpec())
            .build()

        val fileSpec = FileSpec.builder(ROOT_PACKAGE, filename)
            .addType(objectWithConstantsSpec)
            .indent(FOUR_SPACES_IDENTATION)
            .build()

        fileSpec.writeTo(target)
    }

    private fun List<String>.asListPropertySpec(): PropertySpec =
        ClassName("kotlin.collections", "List")
            .parameterizedBy(ClassName("kotlin", "String"))
            .let { parametrizedStringList ->

                val lineByLine = joinToString(
                    separator = LINE_BY_LINE_SEPARATOR,
                    prefix = LINE_BY_LINE_PREFIX,
                    postfix = LINE_BY_LINE_POSTFIX
                ) { name ->
                    name.enclosedWithQuotes()
                }

                val formatted = "\n${ALL_NAMES_TEMPLATE.replace("<items>", lineByLine).trimIndent()}"

                PropertySpec.builder("allAvailable", parametrizedStringList)
                    .initializer(formatted)
                    .build()
            }

    private fun String.enclosedWithQuotes() = "\"$this\""

    private fun List<String>.asConstantsPropertiesSpec(): List<PropertySpec> =
        map { moduleName ->
            PropertySpec.builder(moduleName, String::class, KModifier.CONST)
                .initializer("%S", moduleName)
                .build()
        }

    companion object {
        const val NOT_ACCEPTED_INPUT_FILE = "File should be a directory"
        const val NOT_ACCEPTED_LIST_OF_NAMES = "List of names can not be empty"
        const val FOUR_SPACES_IDENTATION = "    "
        const val LINE_BY_LINE_SEPARATOR = ",\n$FOUR_SPACES_IDENTATION"
        const val LINE_BY_LINE_PREFIX = "\n$FOUR_SPACES_IDENTATION"
        const val LINE_BY_LINE_POSTFIX = "\n"
        const val ROOT_PACKAGE = ""
        const val ALL_NAMES_TEMPLATE = "listOf(<items>)"
    }
}