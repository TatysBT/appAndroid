package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.Movie

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MovieViewModel = viewModel()
            val navController = rememberNavController()
            val movies by viewModel.allMovies.collectAsState()

            NavHost(navController = navController, startDestination = "home") {
                // PANTALLA 1: Lista
                composable("home") {
                    HomeScreen(
                        movies = movies,
                        onMovieClick = { id -> navController.navigate("detail/$id") },
                        onAddClick = { navController.navigate("detail/0") }
                    )
                }

                // PANTALLA 2: Detalle/Formulario
                composable(
                    route = "detail/{movieId}",
                    arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getInt("movieId")
                    MovieEntryScreen(
                        viewModel = viewModel,
                        movieId = if (movieId == 0) null else movieId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(movies: List<Movie>, onMovieClick: (Int) -> Unit, onAddClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Hunger Games Saga") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = Color(0xFFF59E0B)) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        if (movies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay películas. ¡Agrega una!", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(padding)) {
                items(movies) { movie ->
                    MovieItemCard(movie, onClick = { onMovieClick(movie.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieEntryScreen(viewModel: MovieViewModel, movieId: Int?, onNavigateBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var actors by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }

    // Cargar datos si es edición
    LaunchedEffect(movieId) {
        if (movieId != null) {
            val movie = viewModel.getMovie(movieId)
            movie?.let {
                title = it.title
                synopsis = it.synopsis
                actors = it.actors
                releaseDate = it.releaseDate
                platform = it.platform
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (movieId == null) "Nueva Película" else "Editar Película") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Atrás") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = synopsis, onValueChange = { synopsis = it }, label = { Text("Sinopsis") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = actors, onValueChange = { actors = it }, label = { Text("Actores") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = releaseDate, onValueChange = { releaseDate = it }, label = { Text("Fecha") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = platform, onValueChange = { platform = it }, label = { Text("Plataforma") }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (movieId != null) {
                    Button(onClick = { viewModel.deleteMovie(Movie(id = movieId, title, synopsis, actors, releaseDate, platform)); onNavigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color.Red), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, null); Text("Eliminar")
                    }
                }
                Button(onClick = {
                    val m = Movie(id = movieId ?: 0, title, synopsis, actors, releaseDate, platform)
                    if (movieId != null) viewModel.updateMovie(m) else viewModel.addMovie(m)
                    onNavigateBack()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Save, null); Text("Guardar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieItemCard(movie: Movie, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(movie.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Surface(color = Color(0xFFFEF3C7), shape = CircleShape) {
                    Text(movie.releaseDate.take(4), Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF92400E))
                }
            }
            Text(movie.synopsis, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tv, null, tint = Color.Blue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(movie.platform, color = Color.Blue, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}