package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialCyan
import com.example.ui.theme.RoleAdminPurple
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockDanger
import com.example.ui.theme.StockSuccess
import com.example.ui.theme.StockWarning
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun AiSuggestionsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val aiInsight by viewModel.aiInsight.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    var promptText by remember { mutableStateOf("Analyze stock trends and predict optimal buffer quantities") }

    // Auto-generate on first open if empty
    LaunchedEffect(Unit) {
        if (aiInsight == null && !isAiLoading) {
            viewModel.requestAiInsight()
        }
    }

    val presetPrompts = listOf(
        "Predict optimal reorder points for next month",
        "Identify slow-moving and obsolete stock risks",
        "Analyze plant downtime risk from 0-stock spares",
        "Optimize electrical safety buffers and PO batches"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RoleAdminPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RoleAdminPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AI Inventory Suggestion Tool",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Generative AI analysis of transaction trends and optimal reorders",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Custom Prompt Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Slate800)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "PROMPT GENERATIVE AI ADVISOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RoleAdminPurple,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("Ask AI for specific restocking advice or predictions...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_prompt_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoleAdminPurple,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preset prompt pills
                Text(
                    text = "Suggested queries:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(4.dp))

                presetPrompts.forEach { p ->
                    Text(
                        text = "• $p",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = IndustrialCyan,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { promptText = p }
                            .padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.requestAiInsight(promptText) },
                    enabled = !isAiLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_run_ai_analysis"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoleAdminPurple)
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing Inventory Trends...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Advice & Predictions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // AI Insight Display
        aiInsight?.let { insight ->
            val riskColor = when (insight.riskLevel) {
                "CRITICAL" -> StockDanger
                "MODERATE" -> StockWarning
                else -> StockSuccess
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_insight_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(riskColor.copy(alpha = 0.6f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Risk Level Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = riskColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, riskColor)
                        ) {
                            Text(
                                text = insight.riskLevel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Narrative Summary
                    Text(
                        text = insight.summary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFE2E8F0),
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate800, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Recommended Actions
                    Text(
                        text = "RECOMMENDED STRATEGIC ACTIONS:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    insight.recommendedActions.forEach { act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RoleAdminPurple,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = act,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        }
                    }

                    // Predicted Optimal Quantities comparison
                    if (insight.predictedOptimalQuantities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate800, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "PREDICTED OPTIMAL TARGET QUANTITIES:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndustrialCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Mini Table
                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Article Code", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray), modifier = Modifier.weight(1f))
                                    Text("Current", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray), modifier = Modifier.width(60.dp))
                                    Text("AI Target", style = MaterialTheme.typography.labelSmall.copy(color = IndustrialCyan, fontWeight = FontWeight.Bold), modifier = Modifier.width(65.dp))
                                }
                                HorizontalDivider(color = Slate700, modifier = Modifier.padding(vertical = 6.dp))

                                insight.predictedOptimalQuantities.entries.take(8).forEach { (code, target) ->
                                    val current = allProducts.find { it.codeArticle == code }?.quantity ?: 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            code,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "$current",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (current < target) StockWarning else StockSuccess
                                            ),
                                            modifier = Modifier.width(60.dp)
                                        )
                                        Text(
                                            "$target units",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = IndustrialCyan,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.width(65.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
