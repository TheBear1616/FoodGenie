package com.example.foodgenie

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodgenie.ui.theme.FoodGenieTheme
import com.google.firebase.auth.FirebaseAuth

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
                    FoodGenieApp(activity = this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun FoodGenieApp(activity: MainActivity) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "HomeScreen") {
        composable("HomeScreen") {
            HomeScreen(navController = navController, activity)
        }
        composable("RecommenderScreen") {
            RecommenderScreen(navController = navController)
        }
        composable(
            "ResultScreen/{ingredients}",
            arguments = listOf(navArgument("ingredients") { type = NavType.StringType })
        ) { backStackEntry ->
            val ingredients = backStackEntry.arguments?.getString("ingredients") ?: ""
            val context = LocalContext.current
            ResultScreen(navController, ingredients, context)
        }
        composable("FavoritesScreen") {
            FavoritesScreen(navController = navController)
        }
        composable(
            "DisplayRecipeScreen/{recipeID}",
            arguments = listOf(navArgument("recipeID") {type = NavType.StringType})
        ) {backStackEntry ->
            val recipeID = backStackEntry.arguments?.getString("recipeID") ?: ""
            DisplayRecipeScreen(navController = navController, recipeID)
        }
    }
}

@Composable
fun HomeScreen(navController: NavController, activity: MainActivity) {
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

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

        Spacer(modifier = Modifier.height(128.dp))

        Button(
            onClick = { navController.navigate("RecommenderScreen") },
            modifier = Modifier
                .padding(12.dp)
                .height(48.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Recipe Recommender",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White
            )
        }

        Button(
            onClick = { navController.navigate("FavoritesScreen") },
            modifier = Modifier
                .padding(16.dp)
                .height(48.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Favorite Recipes",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(192.dp))

        Button(
            onClick = {
                firebaseAuth.signOut()
                val intent = Intent(activity, Login::class.java)
                activity.startActivity(intent)
            },
            modifier = Modifier
                .padding(12.dp)
                .height(48.dp)
                .fillMaxWidth()
                .background(Color.Red),
            colors = ButtonDefaults.buttonColors(Color.Red)
        ) {
            Text(
                text = "LOGOUT",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}