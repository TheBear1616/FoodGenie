package com.example.foodgenie

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
fun DisplayRecipeScreen(navController: NavController, recipeID: String) {
    var favRecipe = remember { mutableStateOf<FavRecipe?>(null) }
    var isDataLoaded = remember { mutableStateOf(false) }
    val showDeleteButton = favRecipe.value != null
    val dbInstance = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        favRecipe.value = getRecipeById(recipeID)
        isDataLoaded.value = true
        Log.d("result", favRecipe.value.toString())
    }

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
        if (isDataLoaded.value) {
            favRecipe.value?.let { recipe ->
                val processedRecipe = recipe.recipe.replace("\\n", "\n")
                Text(
                    text = processedRecipe,
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(fontSize = 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showDeleteButton) {
                IconButton(
                    onClick = {
                        try {
                            dbInstance.collection("favorite_recipes").document(recipeID).delete()
                        } catch (e: FirebaseFirestoreException) {
                            Log.d("error", "deleteRecipe: $e")
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

suspend fun getRecipeById(recipeId: String): FavRecipe? {
    val dbInstance = FirebaseFirestore.getInstance()
    var favRecipe: FavRecipe? = null

    try {
        val documentSnapshot = dbInstance.collection("favorite_recipes").document(recipeId).get().await()
        if (documentSnapshot.exists()) {
            favRecipe = documentSnapshot.toObject(FavRecipe::class.java)
        }
    } catch (e: FirebaseFirestoreException) {
        Log.d("error", "getDataFromFirestore: $e")
    }

    return favRecipe
}

@Serializable
data class FavRecipe(
    val id: String = "",
    val recipe: String = ""
)