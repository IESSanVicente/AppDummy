package es.javiercarrasco.appdummy

import es.javiercarrasco.appdummy.data.datasource.remote.dto.LibroDto
import es.javiercarrasco.appdummy.data.datasource.remote.dto.toLibro
import es.javiercarrasco.appdummy.data.datasource.remote.dto.toLibros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─── LibroMapperTest.kt - com.ejemplo.appdummy (test) ────────────────────────────────────────────
class LibroMapperTest {

    @Test
    fun `un documento sin key se descarta`() {
        val dto = LibroDto(key = null, title = "Sin identificador")
        assertNull(dto.toLibro())
    }

    @Test
    fun `se prioriza el ISBN de 13 digitos`() {
        val dto = LibroDto(
            key = "/works/OL1W",
            title = "Project Hail Mary",
            isbn = listOf("0593135202", "9780593135204")   // ISBN-10 primero
        )
        assertEquals("9780593135204", dto.toLibro()?.isbn)
    }

    @Test
    fun `varios autores se concatenan separados por comas`() {
        val dto = LibroDto(
            key = "/works/OL2W",
            title = "Buenos presagios",
            authorName = listOf("Terry Pratchett", "Neil Gaiman")
        )
        assertEquals("Terry Pratchett, Neil Gaiman", dto.toLibro()?.autor)
    }

    @Test
    fun `sin author_name se usa el valor por defecto`() {
        val dto = LibroDto(key = "/works/OL3W", title = "Anónimo")
        assertEquals("Autor desconocido", dto.toLibro()?.autor)
    }

    @Test
    fun `sin cover_i la caratula se construye a partir del ISBN`() {
        val dto = LibroDto(
            key = "/works/OL4W",
            title = "Sin portada indexada",
            isbn = listOf("9788418037016"),
            coverId = null
        )
        assertTrue(dto.toLibro()?.cover?.contains("isbn/9788418037016") == true)
    }

    @Test
    fun `los campos locales no se rellenan desde la API`() {
        val libro = LibroDto(key = "/works/OL5W", title = "Cualquiera").toLibro()
        assertEquals(false, libro?.esFavorito)
        assertEquals(false, libro?.leido)
    }

    @Test
    fun `una lista con documentos invalidos solo devuelve los validos`() {
        val documentos = listOf(
            LibroDto(key = "/works/OL6W", title = "Válido"),
            LibroDto(key = null, title = "Sin key"),
            LibroDto(key = "/works/OL7W", title = null)
        )
        assertEquals(1, documentos.toLibros().size)
    }
}