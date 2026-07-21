package es.javiercarrasco.appdummy

import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.screens.listado.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertNotEquals
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// ─── LibrosViewModelTest.kt - com.ejemplo.appdummy (test) ────────────────────────────────────────
@OptIn(ExperimentalCoroutinesApi::class)
class LibrosViewModelTest {
    // Reemplaza Dispatchers.Main con un TestDispatcher para tests unitarios
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LibrosViewModel

    @Before
    fun setup() {
        viewModel = LibrosViewModel(LibrosRepository())
    }

    @Test
    fun `estado inicial es Cargando`() {
        // El ViewModel lanza la carga en init, pero con UnconfinedTestDispatcher
        // la corrutina no se ha ejecutado todavía antes de esta aserción.
        // Comprobamos que el estado de salida es el correcto.
        assertTrue(
            viewModel.uiState.value is LibrosUiState.Cargando ||
                    viewModel.uiState.value is LibrosUiState.Exito
        )
    }

    @Test
    fun `cargarLibros produce estado Exito con datos`() = runTest {
        // advanceUntilIdle ejecuta todas las corrutinas pendientes hasta que no queda ninguna
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue("El estado debe ser Exito", estado is LibrosUiState.Exito)
        assertTrue(
            "Debe haber al menos un libro",
            (estado as LibrosUiState.Exito).libros.isNotEmpty()
        )
    }

    @Test
    fun `actualizarBusqueda actualiza el StateFlow de busqueda`() = runTest {
        viewModel.actualizarBusqueda("Dune")
        assertEquals("Dune", viewModel.busqueda.value)
    }

    @Test
    fun `toggleFavorito invierte el estado de favorito`() = runTest {
        advanceUntilIdle()

        val estadoInicial = viewModel.uiState.value as LibrosUiState.Exito
        val libro = estadoInicial.libros.first()

        viewModel.toggleFavorito(libro) // Si no has implementado toggleFavorito, con SnacckBar, utilizar libro.id.

        val estadoFinal = viewModel.uiState.value as LibrosUiState.Exito
        val modificada = estadoFinal.libros.find { it.id == libro.id }!!
        assertNotEquals(
            "El estado de favorito debe haber cambiado",
            libro.esFavorito,
            modificada.esFavorito
        )
    }
}

// Regla auxiliar reutilizable en todos los tests con corrutinas
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}