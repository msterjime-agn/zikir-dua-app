package com.msterjime.zikirdua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepGreen = Color(0xFF173F35)
private val Green = Color(0xFF2F6B57)
private val SoftGreen = Color(0xFFEAF2EE)
private val Gold = Color(0xFFC8A95B)
private val Ivory = Color(0xFFF8F6EF)
private val Ink = Color(0xFF1F2925)

private val AppColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    background = Ivory,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink
)

enum class AppTab(val title: String, val symbol: String) {
    HOME("Главная", "⌂"),
    PRAYER("Намаз", "☾"),
    DHIKR("Зикр", "✦"),
    TASBIH("Тасбих", "●")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ivory) {
                    ZikirDuaApp()
                }
            }
        }
    }
}

@Composable
private fun ZikirDuaApp() {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = Ivory,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Color.White
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Text(
                                text = tab.symbol,
                                fontSize = 22.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        label = { Text(tab.title, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(
                    onPrayer = { selectedTab = AppTab.PRAYER },
                    onDhikr = { selectedTab = AppTab.DHIKR },
                    onTasbih = { selectedTab = AppTab.TASBIH }
                )
                AppTab.PRAYER -> PrayerScreen()
                AppTab.DHIKR -> DhikrScreen()
                AppTab.TASBIH -> TasbihScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onPrayer: () -> Unit,
    onDhikr: () -> Unit,
    onTasbih: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF0F5F1), Ivory, Ivory)
                )
            )
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text(
                text = "Zikir & Dua",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
            Text(
                text = "Namaz Edition • v2.1",
                fontSize = 14.sp,
                color = Green
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DeepGreen)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📍 Köneürgenç", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Следующий намаз", color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp)
                    Text("—", color = Gold, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Точное время подключим следующим этапом",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Text("Быстрый доступ", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }

        item {
            QuickAction("☾", "Время намаза", "Фаджр • Зухр • Аср • Магриб • Иша", onPrayer)
        }
        item {
            QuickAction("☀", "Утренний азкар", "Чтение после Фаджра", onDhikr)
        }
        item {
            QuickAction("☽", "Вечерний азкар", "Чтение после Магриба", onDhikr)
        }
        item {
            QuickAction("●", "Тасбих", "Счётчик 33 / 100", onTasbih)
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun QuickAction(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SoftGreen, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, fontSize = 24.sp, color = DeepGreen)
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

private data class PrayerRow(val name: String, val time: String)

@Composable
private fun PrayerScreen() {
    val prayers = listOf(
        PrayerRow("Фаджр", "—"),
        PrayerRow("Восход", "—"),
        PrayerRow("Зухр", "—"),
        PrayerRow("Аср", "—"),
        PrayerRow("Магриб", "—"),
        PrayerRow("Иша", "—")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("Время намаза", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("📍 Köneürgenç", color = Green, fontSize = 14.sp)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5D9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Расчёт времени пока не включён. Здесь будут точные времена после подключения метода ТКМ.",
                    modifier = Modifier.padding(14.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF6B5722)
                )
            }
        }
        items(prayers) { prayer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(prayer.name, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text(prayer.time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Green)
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DhikrScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("Зикр и дуа", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text("Выберите раздел", color = Green, fontSize = 14.sp)
        }
        item { SectionCard("☀", "Утренний азкар", "После Фаджра") }
        item { SectionCard("☽", "Вечерний азкар", "После Магриба") }
        item { SectionCard("✦", "После намаза", "Зикр и дуа") }
        item { SectionCard("☾", "Перед сном", "Вечерние дуа") }
        item { SectionCard("♡", "Личная дуа", "Сохранённые обращения") }
    }
}

@Composable
private fun SectionCard(symbol: String, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, fontSize = 27.sp, color = Gold)
            Column(modifier = Modifier.padding(start = 15.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(subtitle, color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TasbihScreen() {
    var count by remember { mutableIntStateOf(0) }
    var target by remember { mutableIntStateOf(33) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        Text("Тасбих", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepGreen)
        Text("Цель: $target", color = Green)
        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DeepGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$count", fontSize = 68.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text("из $target", color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = { count += 1 },
                    modifier = Modifier.size(112.dp),
                    shape = RoundedCornerShape(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen)
                ) {
                    Text("+", fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { target = 33; count = 0 }) { Text("33") }
            Button(onClick = { target = 100; count = 0 }) { Text("100") }
            Button(
                onClick = { count = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green)
            ) { Text("Сброс") }
        }
    }
}
