package com.example.foodgenie

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Composable
fun FavoritesScreen(navController: NavController) {
    var favRecipes = remember { mutableStateOf(listOf<FavRecipes>()) }
    var isDataLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        favRecipes.value = getRecipes()
        isDataLoaded.value = true
        Log.d("result", favRecipes.toString())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Food Genie",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 64.sp
            ),
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.CenterHorizontally)
                .align(Alignment.Start),
            fontFamily = FontFamily.Cursive
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Handle click event */ }
                .padding(16.dp)
                .height(48.dp)
                .background(
                    color = Color(0xFFE81A1A)
                )
        ) {
            Text(
                text = "FAVORITE LIST:",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isDataLoaded.value) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(favRecipes.value) { index, recipe ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Handle click event */ }
                                .height(48.dp)
                                .background(
                                    color = Color(android.graphics.Color.parseColor("#08648c")),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            Text(
                                text = recipe.id,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp) // Adjust the desired height here, should match padding in Box
                        .background(Color.White)
                        .align(Alignment.BottomCenter)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

suspend fun getRecipes(): List<FavRecipes> {
    val dbInstance = FirebaseFirestore.getInstance()
    var favRecipes = listOf<FavRecipes>()

    try {
        favRecipes = dbInstance.collection("favorite_recipes").get().await().map {
            it.toObject(FavRecipes::class.java)
        }
    } catch (e: FirebaseFirestoreException) {
        Log.d("error", "getDataFromFireStore: $e")
    }

    return favRecipes
}

@Serializable
data class FavRecipes(
    val id: String = "",
    val recipe: String = ""
)