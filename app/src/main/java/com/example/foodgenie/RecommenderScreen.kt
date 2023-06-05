package com.example.foodgenie

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommenderScreen(navController: NavController) {
    val textState = remember { mutableStateOf("") }
    val responseTextState = remember { mutableStateOf("") }
    val isLoadingState = remember { mutableStateOf(false) }
    val ingredientListState = remember { mutableStateOf(emptyList<String>()) }
    lateinit var firebaseAuth: FirebaseAuth


    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Food Genie",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 64.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = FontFamily.Cursive
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            label = { Text("Please enter the ingredients you have.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    textState.value = ""
                    ingredientListState.value = emptyList()
                },
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    if (textState.value.isNotEmpty()) {
                        ingredientListState.value =
                            ingredientListState.value + textState.value
                        textState.value = ""
                    }
                },
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingState.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (responseTextState.value.isNotEmpty()) {
                Text(
                    text = responseTextState.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (ingredientListState.value.isNotEmpty()) {
            Text(
                text = "Ingredient List:",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .border(1.dp, Color.Black)
                    .padding(8.dp)
                    .fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                items(ingredientListState.value) { ingredient ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$ingredient",
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                ingredientListState.value =
                                    ingredientListState.value.filter { it != ingredient }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Remove",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val newIngredientList =
                    ingredientListState.value.toMutableList() + textState.value
                textState.value = ""
                val ingredients = newIngredientList.joinToString(", ")
                if (ingredients.isNotEmpty()) {
                    makeApiRequest(ingredients) { result ->
                        responseTextState.value = result
                        isLoadingState.value = false
                        ingredientListState.value = emptyList()
                    }
                    isLoadingState.value = true
                }
            },
            modifier = Modifier
                .padding(12.dp)
                .height(48.dp)
                .fillMaxWidth()
        ) {
            Text("Get Recipes")
        }
    }
}