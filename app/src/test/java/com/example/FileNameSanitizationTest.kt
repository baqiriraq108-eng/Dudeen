package com.example

import com.example.util.ExportSharingManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileNameSanitizationTest {

    @Test
    fun `test default file name produces Suchverlauf_Deutsch without leading dot`() {
        val base = ExportSharingManager.sanitizeFileName("")
        assertEquals("Suchverlauf_Deutsch", base)

        val pdfName = ExportSharingManager.buildFileName(base, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", pdfName)
        assertFalse("Filename must not start with a dot", pdfName.startsWith("."))
        assertTrue("Filename must start with a letter", pdfName.first().isLetter())

        val xlsxName = ExportSharingManager.buildFileName(base, ExportSharingManager.ExportFormat.XLSX)
        assertEquals("Suchverlauf_Deutsch.xlsx", xlsxName)
        assertFalse("Filename must not start with a dot", xlsxName.startsWith("."))
        assertTrue("Filename must start with a letter", xlsxName.first().isLetter())
    }

    @Test
    fun `test leading dot is removed from filename`() {
        val inputWithDot = ".Suchverlauf_Deutsch"
        val sanitized = ExportSharingManager.sanitizeFileName(inputWithDot)
        assertEquals("Suchverlauf_Deutsch", sanitized)

        val pdfName = ExportSharingManager.buildFileName(inputWithDot, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", pdfName)
        assertFalse("Filename must not start with a dot", pdfName.startsWith("."))

        val xlsxName = ExportSharingManager.buildFileName(inputWithDot, ExportSharingManager.ExportFormat.XLSX)
        assertEquals("Suchverlauf_Deutsch.xlsx", xlsxName)
        assertFalse("Filename must not start with a dot", xlsxName.startsWith("."))
    }

    @Test
    fun `test multiple leading dots and extensions are sanitized properly`() {
        val input = "...Suchverlauf_Deutsch.pdf"
        val sanitized = ExportSharingManager.sanitizeFileName(input)
        assertEquals("Suchverlauf_Deutsch", sanitized)

        val pdfName = ExportSharingManager.buildFileName(input, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", pdfName)
    }

    @Test
    fun `test dot only or extension only inputs fallback to default name without leading dot`() {
        val dotInput = "."
        val sanitizedDot = ExportSharingManager.sanitizeFileName(dotInput)
        assertEquals("Suchverlauf_Deutsch", sanitizedDot)

        val pdfOnly = ".pdf"
        val sanitizedPdf = ExportSharingManager.sanitizeFileName(pdfOnly)
        assertEquals("Suchverlauf_Deutsch", sanitizedPdf)

        val xlsxOnly = ".xlsx"
        val sanitizedXlsx = ExportSharingManager.sanitizeFileName(xlsxOnly)
        assertEquals("Suchverlauf_Deutsch", sanitizedXlsx)

        val finalPdf = ExportSharingManager.buildFileName(pdfOnly, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", finalPdf)
        assertFalse(finalPdf.startsWith("."))
    }

    @Test
    fun `test custom user name is preserved without leading dot`() {
        val customName = "Meine_Wortliste"
        val sanitized = ExportSharingManager.sanitizeFileName(customName)
        assertEquals("Meine_Wortliste", sanitized)

        val pdfName = ExportSharingManager.buildFileName(customName, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Meine_Wortliste.pdf", pdfName)

        val xlsxName = ExportSharingManager.buildFileName(customName, ExportSharingManager.ExportFormat.XLSX)
        assertEquals("Meine_Wortliste.xlsx", xlsxName)
    }

    @Test
    fun `test notebook name is included in file name`() {
        val xlsxWithNotebook = ExportSharingManager.buildFileName(
            baseName = "Suchverlauf_Deutsch",
            format = ExportSharingManager.ExportFormat.XLSX,
            notebookName = "B2_Prüfung"
        )
        assertEquals("Suchverlauf_Deutsch_B2_Prüfung.xlsx", xlsxWithNotebook)

        // If base name already has notebook name, avoid duplicating
        val alreadyHasIt = ExportSharingManager.buildFileName(
            baseName = "Suchverlauf_B2_Prüfung",
            format = ExportSharingManager.ExportFormat.XLSX,
            notebookName = "B2_Prüfung"
        )
        assertEquals("Suchverlauf_B2_Prüfung.xlsx", alreadyHasIt)
    }

    @Test
    fun `test stripNotebookPrefix and buildNotebookFileName keep in-app names clean`() {
        assertEquals("A1.1", ExportSharingManager.stripNotebookPrefix("Notebook_A1.1.json"))
        assertEquals("A1.1", ExportSharingManager.stripNotebookPrefix("Notebook_A1.1"))
        assertEquals("Allgemein", ExportSharingManager.stripNotebookPrefix("notebook_Allgemein.JSON"))
        assertEquals("A1.1", ExportSharingManager.stripNotebookPrefix("A1.1.json"))

        assertEquals("Notebook_A1.1.json", ExportSharingManager.buildNotebookFileName("A1.1"))
        assertEquals("Notebook_A1.1.json", ExportSharingManager.buildNotebookFileName("Notebook_A1.1.json"))
        assertEquals("Notebook_Allgemein.json", ExportSharingManager.buildNotebookFileName("Allgemein"))
    }

    @Test
    fun `test buildDefaultExportBaseName produces PDF and Excel date-prefixed names`() {
        // Fixed timestamp: 2026-11-15T12:00:00Z
        val cal = java.util.Calendar.getInstance(java.util.Locale.US).apply {
            set(2026, java.util.Calendar.NOVEMBER, 15, 12, 0, 0)
        }
        val ts = cal.timeInMillis

        val pdfBase = ExportSharingManager.buildDefaultExportBaseName(
            format = ExportSharingManager.ExportFormat.PDF,
            notebookName = "Notebook_A1.1",
            timestampMillis = ts
        )
        assertEquals("PDF_A1.1_2026-11-15", pdfBase)
        assertEquals(
            "PDF_A1.1_2026-11-15.pdf",
            ExportSharingManager.buildFileName(pdfBase, ExportSharingManager.ExportFormat.PDF, "A1.1")
        )

        val excelBase = ExportSharingManager.buildDefaultExportBaseName(
            format = ExportSharingManager.ExportFormat.XLSX,
            notebookName = "Allgemein",
            timestampMillis = ts
        )
        assertEquals("Excel_Allgemein_2026-11-15", excelBase)
        assertEquals(
            "Excel_Allgemein_2026-11-15.xlsx",
            ExportSharingManager.buildFileName(excelBase, ExportSharingManager.ExportFormat.XLSX, "Allgemein")
        )
    }
}
