package com.example.foodgenie

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ResultScreen(navController: NavController, ingredients: String, context: Context) {
    val responseTextState = remember { mutableStateOf("") }
    val isLoadingState = remember { mutableStateOf(false) }
    val showFavoriteButton = responseTextState.value.isNotEmpty()
    val dbInstance = FirebaseFirestore.getInstance()

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
            makeApiRequest(ingredients) { result ->
                responseTextState.value = result
                isLoadingState.value = false
            }
            isLoadingState.value = true
        }

        if (isLoadingState.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            val dishName = responseTextState.value.substringAfter("Dish: ").substringBefore("\n")
            val recipeToAdd = hashMapOf(
                "id" to dishName,
                "recipe" to responseTextState.value
            )

            if (showFavoriteButton) {
                Text(
                    text = responseTextState.value,
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                IconButton(
                    onClick = {
                        dbInstance.collection("favorite_recipes")
                            .add(recipeToAdd)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(context, "Successfully Saved To Favorites", Toast.LENGTH_SHORT).show()
                                Log.d("success", "DocumentSnapshot added with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to save the dish to favorites. Please try again.", Toast.LENGTH_SHORT).show()
                                Log.w("error", "Error adding document", e)
                            }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

fun makeApiRequest(ingredients: String, callback: (String) -> Unit) {
    val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS) // Increase the overall timeout to 60 seconds
        .build()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = """
    {
        "model": "gpt-3.5-turbo",
        "messages": [
            {
                "role": "user",
                "content": "I have $ingredients. What dish can I make out of the mentioned ingredients? Please provide me with the recipe as well. Provide the output in the following format: Dish, Ingredients, and Instructions."
            }
        ],
        "temperature": 1,
        "top_p": 1,
        "n": 1,
        "stream": false,
        "max_tokens": 250,
        "presence_penalty": 0,
        "frequency_penalty": 0 
    }
""".trimIndent().toRequestBody(mediaType)
    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .post(requestBody)
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .addHeader("Authorization", "Bearer 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")
        .build()


    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response: Response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            launch(Dispatchers.Main) {
                responseBody?.let {
                    val json = Json { ignoreUnknownKeys = true }
                    val apiResponse = json.decodeFromString<APIResponse>(it)
                    val messageContent =
                        apiResponse.choices.firstOrNull()?.message?.content ?: "No response"
                    callback(messageContent)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun generateRandom4DigitNumber(): Int {
    return Random.nextInt(1000, 10000)
}

@Serializable
data class APIResponse(
    val id: String? = null,
    @SerialName("object")
    val responseObject: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val usage: Usage? = null,
    val choices: List<Choice>
)


@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

@Serializable
data class Choice(
    val message: Message,
    val finish_reason: String,
    val index: Int
)

@Serializable
data class Message(
    val role: String,
    val content: String
)