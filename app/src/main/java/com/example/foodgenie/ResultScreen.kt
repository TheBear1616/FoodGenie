package com.example.foodgenie

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

@Composable
fun ResultScreen(navController: NavController) {
    val responseTextState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val ingredients =
            "protein powder, cinnamon, eggs, milk, bread, oats, and strawberries"

        LaunchedEffect(Unit) {
            makeApiRequest(ingredients) { result ->
                responseTextState.value = result
            }
        }

//        Button(
//            onClick = {
//                val newIngredientList =
//                    ingredientListState.value.toMutableList() + textState.value
//                textState.value = ""
//                val ingredients = newIngredientList.joinToString(", ")
//                if (ingredients.isNotEmpty()) {
//                    makeApiRequest(ingredients) { result ->
//                        responseTextState.value = result
//                        isLoadingState.value = false
//                        ingredientListState.value = emptyList()
//                    }
//                    isLoadingState.value = true
//                }
//            },
//            modifier = Modifier
//                .padding(12.dp)
//                .height(48.dp)
//                .fillMaxWidth()
//        ) {
//            Text("Get Recipes")
//        }

        Text(text = responseTextState.value)
        Spacer(modifier = Modifier.height(16.dp))


        IconButton(
            onClick = { },
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

fun makeApiRequest(ingredients: String, callback: (String) -> Unit) {
    val client = OkHttpClient()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = """
    {
        "model": "gpt-3.5-turbo",
        "messages": [
            {
                "role": "user",
                "content": "I have $ingredients. What dish can I make out of the mentioned ingredients for my breakfast? Please provide me with the recipe as well. Provide the output in the following format: Dish, Ingredients, and Instructions."
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