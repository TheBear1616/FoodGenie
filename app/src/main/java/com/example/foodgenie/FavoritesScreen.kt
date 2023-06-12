package com.example.foodgenie

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Composable
fun FavoritesScreen(navController: NavController) {
    var favRecipes = listOf<FavRecipes>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
            .border(5.dp, Color(0xFFE81A1A), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(Unit) {
            favRecipes = getRecipes()
            Log.d("result", favRecipes.toString())
        }

        Text(
            text = favRecipes.joinToString(", "),
            modifier = Modifier.padding(16.dp),
            style = TextStyle(fontSize = 16.sp)
        )
        Spacer(modifier = Modifier.height(16.dp))
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