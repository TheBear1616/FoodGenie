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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
                    FoodGenieApp()
                }
            }
        }
    }
}

@Composable
fun FoodGenieAppTest() {
    var text by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var ingredientList by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Food Genie",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Cursive,
                fontSize = 64.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
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
                    text = ""
                    ingredientList = emptyList()
                },
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    if (text.isNotEmpty()) {
                        ingredientList = ingredientList + text
                        text = ""
                    }
                },
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (responseText.isNotEmpty()) {
                Text(
                    text = responseText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(16.dp)
                )
            }
        }

        if (ingredientList.isNotEmpty()) {
            Text(
                text = "Ingredients List",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            items(ingredientList) { ingredient ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "$ingredient",
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            ingredientList = ingredientList.filter { it != ingredient }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                        content = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Remove",
                                tint = Color.Red
                            )
                        }
                    )
                }

            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (text.isNotEmpty()) {
                    makeApiRequest(text) { result ->
                        responseText = result
                        isLoading = false
                    }
                    isLoading = true
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
                                ingredientListState.value = ingredientListState.value.filter { it != ingredient }
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
                if (textState.value.isNotEmpty()) {
                    makeApiRequest(textState.value) { result ->
                        responseTextState.value = result
                        isLoadingState.value = false
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
    val mediaType = "application/json".toMediaType()
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
                    val chatResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ChatResponse>(it)
                    val message = chatResponse.messages?.firstOrNull()
                    val result = message?.content ?: "No response"
                    callback(result)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

@Serializable
data class ChatResponse(
    val messages: List<Message>? = null
)

@Serializable
data class Message(
    val role: String,
    val content: String
)