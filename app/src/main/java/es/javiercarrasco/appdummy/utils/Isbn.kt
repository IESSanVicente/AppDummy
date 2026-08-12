package es.javiercarrasco.appdummy.utils

// ─── utils/Isbn.kt ────────────────────────────────────────────────────────────────────────────────
object Isbn {

    // Elimina guiones y espacios: "978-84-1803-701-6" → "9788418037016"
    fun normalizar(texto: String): String = texto.filter { it.isDigit() }

    fun esValido13(texto: String): Boolean {
        val digitos = normalizar(texto)
        if (digitos.length != 13) return false

        // Pesos alternos 1, 3, 1, 3... sobre los 13 dígitos
        val suma = digitos.mapIndexed { indice, caracter ->
            (caracter - '0') * if (indice % 2 == 0) 1 else 3
        }.sum()

        return suma % 10 == 0
    }
}