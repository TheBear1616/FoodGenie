package com.example.foodgenie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodgenie.ui.theme.FoodGenieTheme
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodGenieTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiRequestScreen()
                }
            }
        }
    }
}

@Composable
fun ApiRequestScreen() {
    val responseTextState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val ingredients =
                    "protein powder, cinnamon, eggs, milk, bread, oats, and strawberries"
                makeApiRequest(ingredients) { result ->
                    responseTextState.value = result
                }
            },
            modifier = Modifier.padding(12.dp)
        ) {
            Text("Make API Request")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = responseTextState.value)
    }
}

@Composable
fun FoodGenieApp() {
    val textState = remember { mutableStateOf("") }
    val responseTextState = remember { mutableStateOf("") }
    val isLoadingState = remember { mutableStateOf(false) }
    val ingredientListState = remember { mutableStateOf(emptyList<String>()) }

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
                        ingredientListState.value = ingredientListState.value + textState.value
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
                val newIngredientList = ingredientListState.value.toMutableList() + textState.value
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FoodGenieTheme {
        FoodGenieApp()
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