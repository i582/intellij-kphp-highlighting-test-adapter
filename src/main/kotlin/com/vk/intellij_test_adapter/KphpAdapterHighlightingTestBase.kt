package com.vk.intellij_test_adapter

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

/**
 * The base class for the highlighting test in the KPHP format.
 */
abstract class KphpAdapterHighlightingTestBase : BasePlatformTestCase() {
    /**
     * Describes the file extensions to be tested.
     */
    abstract val testFileExtensions: List<String>

    /**
     * Describes the folder relative to which the path passed to [runTest] will resolve.
     */
    abstract val testBaseFolder: String

    /**
     * Describes the folder where the generated files for testing will be stored.
     */
    abstract val testGenFolder: String

    /**
     * Describes the amount spaces to replace tabs with.
     */
    abstract val tabSize: Int

    /**
     * Describes the beginning of the comment to be processed.
     *
     *   - `//` for PHP.
     *   - `#` for Python or YAML.
     *   - e.g.
     */
    protected val commentStart: String = "//"

    /**
     * Describes the inspections that will be checked by the test.
     */
    abstract val inspections: List<LocalInspectionTool>

    override fun getTestDataPath(): String = testGenFolder

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(*inspections.toTypedArray())
    }

    fun runTest(dir: String) {
        val ok = copyTestFiles(dir)
        if (!ok) {
            return
        }

        walkFiles(dir)

        testHighlighting(dir)
    }

    private fun testHighlighting(dir: String) {
        LocalFileSystem.getInstance().refresh(false)

        val genTestDataFolder = File(testGenFolder, dir)
        val walker = genTestDataFolder.walk()
        val files = walker
            .filter { it.isFile && it.extension in testFileExtensions }
            .map { it.path.removePrefix("$testGenFolder/") }
            .toList().toTypedArray()

        myFixture.configureByFiles(*files)

        files.forEach {
            val file = myFixture.findFileInTempDir(it) ?: return@forEach
            myFixture.openFileInEditor(file)
            myFixture.checkHighlighting(true, false, true)
        }
    }

    private fun walkFiles(dir: String) {
        LocalFileSystem.getInstance().refresh(false)

        val genTestDataFolder = File(testGenFolder, dir)

        val walker = genTestDataFolder.walk()
        walker
            .filter { it.isFile && it.extension in testFileExtensions }
            .forEach { handleFile(it) }
    }

    private fun handleFile(file: File) {
        val lines = file.readText().lines().toMutableList()
        val tab = " ".repeat(tabSize)

        var i = 1
        while (i < lines.size) {
            val cursor = lines[i].replace("\t", tab)
            val trimmed = cursor.trim()

            if (!trimmed.startsWith(commentStart) || !trimmed.contains("^")) {
                i++
                continue
            }

            val lineWithCodeIndex = i - 1
            val lineWithCode = lines[lineWithCodeIndex].replace("\t", tab)
            val lineWithError = lines[i + 1]
            val errorMessageRaw = lineWithError
                .substringAfter(commentStart)
                .trim()

            if (!errorMessageRaw.contains(':')) {
                return
            }

            val severity = errorMessageRaw.substringBefore(':').trim()
            var errorMessage = errorMessageRaw.substringAfter(':').trim()

            while (true) {
                val line = lines[i + 2].trim()
                if (!line.startsWith(commentStart)) {
                    break
                }

                errorMessage += "\n" + line
                    .substringAfter(commentStart)
                    .trim()

                i++
            }

            val startIndex = cursor.indexOf('^')
            val lastIndex = cursor.lastIndexOf('^')

            val newLine = lineWithCode.substring(0, startIndex) +
                    "<$severity descr=\"$errorMessage\">" +
                    lineWithCode.substring(startIndex, lastIndex + 1) +
                    "</$severity>" +
                    lineWithCode.substring(lastIndex + 1)

            lines[lineWithCodeIndex] = newLine

            i++
        }

        file.writeText(lines.joinToString("\n"))
    }

    private fun copyTestFiles(dir: String): Boolean {
        val testDataFolder = File(testBaseFolder, dir)
        val genTestDataFolder = File(testGenFolder, dir)
        try {
            testDataFolder.copyRecursively(genTestDataFolder, overwrite = true)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }
}
