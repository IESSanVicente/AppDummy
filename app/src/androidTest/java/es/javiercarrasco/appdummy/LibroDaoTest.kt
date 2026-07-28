package es.javiercarrasco.appdummy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.javiercarrasco.appdummy.data.datasource.local.AppDatabase
import es.javiercarrasco.appdummy.data.datasource.local.LibrosDao
import es.javiercarrasco.appdummy.data.model.Libro
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// ─── LibroDaoTest.kt - com.ejemplo.appdummy (androidTest) ────────────────────────────────────────
@RunWith(AndroidJUnit4::class)
class LibroDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: LibrosDao

    @Before
    fun crearBaseDeDatos() {
        // inMemoryDatabaseBuilder: BD temporal en RAM para tests
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()   // solo en tests — simplifica la escritura
            .build()
        dao = database.libroDao()
    }

    @After
    fun cerrarBaseDeDatos() {
        database.close()
    }

    @Test
    fun insertarYRecuperarLibro() = runTest {
        val libro = Libro(
            id = "550",
            titulo = "Proyecto Hail Mary",
            autor = "Andy Weir",
            year = 2021,
            isbn = "9788418037016",
            cover = "https://covers.openlibrary.org/b/isbn/9788418037016-L.jpg"
        )
        dao.upsert(libro)

        val resultado = dao.obtenerPorId("550")
        assertEquals("Proyecto Hail Mary", resultado?.titulo)
        assertFalse(resultado!!.esFavorito)   // por defecto es false
    }

    @Test
    fun toggleFavoritaInvierteElEstado() = runTest {
        val libro = Libro(
            id = "2",
            titulo = "Juego de tronos",
            autor = "George R.R. Martin",
            year = 1996,
            isbn = "9780307951182",
            cover = "https://covers.openlibrary.org/b/isbn/9780307951182-L.jpg"
        )
        dao.upsert(libro)

        // Primera llamada: false → true
        dao.toggleFavorito("2")
        assertTrue(dao.obtenerPorId("2")!!.esFavorito)

        // Segunda llamada: true → false
        dao.toggleFavorito("2")
        assertFalse(dao.obtenerPorId("2")!!.esFavorito)
    }

    @Test
    fun upsertConservandoFavoritoNoSobreescribeFavorito() = runTest {
        // 1. Insertar libro con esFavorito = true
        val libroOriginal = Libro(
            "3",
            "Festín de cuervos",
            "George R.R. Martin",
            2005,
            "9780307951212",
            "https://covers.openlibrary.org/b/isbn/9780307951212-L.jpg",
            true,
            false
        )
        dao.upsert(libroOriginal)

        // 2. Llamar a upsertConservandoFavorito con el mismo id pero esFavorito = false
        //    (como llegaría de la API)
        val libroDeApi = libroOriginal.copy(
            titulo = "Festín de cisnes", // título actualizado
            year = 2026, // año actualizado
            esFavorito = false   // la API no sabe que el usuario la marcó como favorito
        )
        dao.upsertConservandoFavorito(listOf(libroDeApi))

        // 3. Verificar que el título se actualizó pero esFavorita sigue siendo true
        val resultado = dao.obtenerPorId("3")
        assertEquals("Festín de cisnes", resultado?.titulo)
        assertEquals(2026, resultado?.year)
        assertTrue(resultado!!.esFavorito)   // ✅ se conservó el favorito del usuario
    }

    @Test
    fun observarFavoritasEmiteSoloLasMarcadas() = runTest {
        // Insertar varios libros, algunos favoritos
        dao.upsert(
            Libro(
                id = "1",
                titulo = "Proyecto Hail Mary",
                autor = "Andy Weir",
                year = 2021,
                isbn = "9788418037016",
                cover = "https://covers.openlibrary.org/b/isbn/9788418037016-L.jpg",
                esFavorito = true,
                leido = false
            )
        )
        dao.upsert(
            Libro(
                id = "2",
                titulo = "Juego de tronos",
                autor = "George R.R. Martin",
                year = 1996,
                isbn = "9780307951182",
                cover = "https://covers.openlibrary.org/b/isbn/9780307951182-L.jpg",
                esFavorito = true,
                leido = true
            )
        )
        dao.upsert(
            Libro(
                "3",
                "Festín de cuervos",
                "George R.R. Martin",
                2005,
                "9780307951212",
                "https://covers.openlibrary.org/b/isbn/9780307951212-L.jpg",
                false,
                false
            ),
        )

        // first() recoge el primer valor emitido por el Flow y cancela la colección
        val favoritas = dao.observarFavoritos().first()
        assertEquals(2, favoritas.size)
        assertTrue(favoritas.all { it.esFavorito })
    }
}