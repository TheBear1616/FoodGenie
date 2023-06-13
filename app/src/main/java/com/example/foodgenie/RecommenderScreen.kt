package com.example.foodgenie

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.navigation.NavController

@Composable
fun RecommenderScreen(navController: NavController) {
    val textState = remember { mutableStateOf("") }
    val ingredientListState = remember { mutableStateOf(emptyList<String>()) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

        if (ingredientListState.value.isNotEmpty()) {
            Text(
                text = "INGREDIENT LIST:",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .border(2.dp, Color(0xFFE81A1A), shape = RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .weight(7f)
                    .border(2.dp, Color(0xFFE81A1A), shape = RoundedCornerShape(12.dp)),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
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
                                imageVector = Icons.Default.Delete,
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
                navController.navigate("ResultScreen/${Uri.encode(ingredients)}")
            },
            modifier = Modifier
                .padding(12.dp)
                .height(48.dp)
                .fillMaxWidth()
        ) {
            Text("GET RECIPES")
        }
    }
}