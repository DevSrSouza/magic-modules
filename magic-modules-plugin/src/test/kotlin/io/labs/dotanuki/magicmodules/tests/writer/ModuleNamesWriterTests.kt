package io.labs.dotanuki.magicmodules.tests.writer

import io.labs.dotanuki.magicmodules.writer.ModuleNamesWriter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class ModuleNamesWriterTests {

    @get:Rule val tempFolder = TemporaryFolder()

    private lateinit var writer: ModuleNamesWriter

    @Before fun `before each test`() {
        writer = ModuleNamesWriter()
    }

    @Test fun `should not write on non-directory file`() {
        val target = tempFolder.newFile()
        val names = listOf("library1", "library2")
        val filename = "Libraries"

        val execution = { writer.write(target, filename, names) }

        val expected = IllegalArgumentException::class.java
        assertThatThrownBy(execution).isInstanceOf(expected)
    }

    @Test fun `should not write when no names are provided`() {
        val target = tempFolder.newFolder()
        val names = emptyList<String>()
        val filename = "Modules"

        val execution = { writer.write(target, filename, names) }

        val expected = IllegalArgumentException::class.java
        assertThatThrownBy(execution).isInstanceOf(expected)
    }

    @Test fun `write one module library`() {
        val target = tempFolder.newFolder()
        val names = listOf("core", "common")
        val filename = "Modules"

        writer.write(target, filename, names)

        val writtenCode = target.resolve("$filename.kt").readText()
        val expectedCode = """
            import kotlin.String
            import kotlin.collections.List
            
            object Modules {
                const val core: String = "core"

                const val common: String = "common"

                val allAvailable: List<String> = 
                        listOf(
                            "core",
                            "common"
                        )
            }
            
            """.trimIndent()

        assertThat(writtenCode).isEqualTo(expectedCode)
    }
}