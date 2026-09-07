package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AgendaTab

@Composable
fun HeaderBar(
    currentTab: AgendaTab,
    onTabSelected: (AgendaTab) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Row: Logo, Title, Brand, Theme toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo box respecting exact user image in high definition - Clickable to expand!
                    Box(
                        modifier = Modifier
                            .testTag("header_logo_button")
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFFBEB))
                            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                            .clickable { onLogoClick() }
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_hogar),
                            contentDescription = "Logo Agenda Hogares (Tocar para ampliar)",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title with 3D format and expanded size
                    Box(
                        modifier = Modifier.padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // 3D Extrusion Layer 2 (Deep celeste shadow matching logo frame theme)
                        Text(
                            text = "AGENDA HOGARES",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0284C7),
                            letterSpacing = 1.sp,
                            modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                        )
                        // 3D Extrusion Layer 1 (Celeste shadow matching the exact logo frame color #38BDF8)
                        Text(
                            text = "AGENDA HOGARES",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp,
                            modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                        )
                        // 3D Front Face
                        Text(
                            text = "AGENDA HOGARES",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1B5E20),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Theme Toggle
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .testTag("theme_toggle_button")
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDarkMode) Color(0xFF334155) else Color(0xFFFEF3C7)
                        )
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Modo Claro/Oscuro",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs with compact icon-only buttons to fit on screen without horizontal scrolling
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AgendaTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    ElevatedButton(
                        onClick = { onTabSelected(tab) },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}")
                    ) {
                        when (tab) {
                            AgendaTab.ACTIVIDADES -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_nav_tareas),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AgendaTab.OPERADORES -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_nav_operadores),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AgendaTab.PLANILLA -> {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AgendaTab.CRONOGRAMA -> {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AgendaTab.CHICOS -> {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AgendaTab.LUGARES -> {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
