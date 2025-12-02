package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Movie
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.movieDao()

    // Lista de películas siempre actualizada
    val allMovies: StateFlow<List<Movie>> = dao.getAllMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Bloque de inicialización: Precargar datos si la BD está vacía
        viewModelScope.launch {
            val currentMovies = dao.getAllMovies().first()
            if (currentMovies.isEmpty()) {
                preloadData()
            }
        }
    }

    private fun preloadData() {
        val initialMovies = listOf(
            Movie(
                title = "Los Juegos del Hambre",
                synopsis = "Katniss Everdeen toma voluntariamente el lugar de su hermana menor en los Juegos del Hambre: una competición televisada en la que dos adolescentes de cada uno de los doce Distritos de Panem son elegidos al azar para luchar hasta la muerte.",
                actors = "Jennifer Lawrence, Josh Hutcherson, Liam Hemsworth",
                releaseDate = "2012-03-23",
                platform = "Netflix, HBO Max"
            ),
            Movie(
                title = "En Llamas",
                synopsis = "Katniss Everdeen y Peeta Mellark se convierten en objetivos del Capitolio después de que su victoria en los 74º Juegos del Hambre desencadena una rebelión en los Distritos de Panem.",
                actors = "Jennifer Lawrence, Josh Hutcherson, Philip Seymour Hoffman",
                releaseDate = "2013-11-22",
                platform = "Amazon Prime Video"
            ),
            Movie(
                title = "Sinsajo - Parte 1",
                synopsis = "Katniss Everdeen se encuentra en el Distrito 13 después de destrozar los juegos para siempre. Bajo el liderazgo de la Presidenta Coin y el consejo de sus amigos de confianza, Katniss expande sus alas mientras lucha por salvar a Peeta.",
                actors = "Jennifer Lawrence, Josh Hutcherson, Liam Hemsworth",
                releaseDate = "2014-11-21",
                platform = "Apple TV+"
            ),
            Movie(
                title = "Sinsajo - Parte 2",
                synopsis = "Katniss y un equipo de rebeldes del Distrito 13 se preparan para la batalla final que decidirá el destino de Panem.",
                actors = "Jennifer Lawrence, Josh Hutcherson, Julianne Moore",
                releaseDate = "2015-11-20",
                platform = "Apple TV+"
            ),
            Movie(
                title = "Balada de Pájaros Cantores y Serpientes",
                synopsis = "Coriolanus Snow es el mentor y desarrolla sentimientos por la tributo del Distrito 12 durante los décimos Juegos del Hambre.",
                actors = "Tom Blyth, Rachel Zegler, Peter Dinklage",
                releaseDate = "2023-11-17",
                platform = "Cines, Prime Video"
            )
        )

        viewModelScope.launch {
            initialMovies.forEach { dao.insert(it) }
        }
    }

    fun addMovie(movie: Movie) {
        viewModelScope.launch { dao.insert(movie) }
    }

    fun updateMovie(movie: Movie) {
        viewModelScope.launch { dao.update(movie) }
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch { dao.delete(movie) }
    }

    suspend fun getMovie(id: Int): Movie? {
        return dao.getMovieById(id)
    }
}