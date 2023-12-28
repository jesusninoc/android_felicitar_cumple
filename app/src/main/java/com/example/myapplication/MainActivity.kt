package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BirthdayApp()
        }
    }
}

@Composable
fun BirthdayApp() {
    val context = LocalContext.current
    var personsWithBirthdayToday by remember { mutableStateOf<List<Person>>(emptyList()) }

    LaunchedEffect(true) {
        personsWithBirthdayToday = withContext(Dispatchers.IO) {
            findPeopleWithBirthdayToday(context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Personas con cumpleaños hoy:")
        personsWithBirthdayToday.forEach { person ->
            Text(person.nombre)
        }
    }
}

fun findPeopleWithBirthdayToday(context: android.content.Context): List<Person> {
    val jsonString = readJsonFromUrl("https://www.jesusninoc.com/cumple.json")

    val gson = Gson()
    val listType = object : TypeToken<List<Person>>() {}.type
    val persons: List<Person> = gson.fromJson(jsonString, listType)

    val currentDate = Calendar.getInstance().time
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val peopleWithBirthdayToday = mutableListOf<Person>()

    persons.forEach { person ->
        val birthDate = dateFormatter.parse(person.fecha_nacimiento)
        if (birthDate != null && isSameDayAndMonth(birthDate, currentDate)) {
            peopleWithBirthdayToday.add(person)
        }
    }
    return peopleWithBirthdayToday
}

fun readJsonFromUrl(url: String): String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    val response: Response
    try {
        response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
}

fun isSameDayAndMonth(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }

    val dateFormatter = SimpleDateFormat("dd-MM", Locale.getDefault())

    val dayAndMonth1 = dateFormatter.format(cal1.time)
    val dayAndMonth2 = dateFormatter.format(cal2.time)

    return dayAndMonth1 == dayAndMonth2
}

data class Person(
    val nombre: String,
    val fecha_nacimiento: String
)