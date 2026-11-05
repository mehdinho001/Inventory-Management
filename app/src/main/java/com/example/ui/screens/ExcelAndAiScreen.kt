package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialCyan
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun ExcelAndAiScreen(
    viewModel: InventoryViewModel,
    initialSubTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(initialSubTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = Slate900,
            contentColor = IndustrialCyan,
            modifier = Modifier.fillMaxWidth().testTag("excel_ai_subtab")
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Excel File Sync & Reports", fontSize = 13.sp) },
                icon = { Icon(Icons.Default.TableChart, contentDescription = null) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("AI Inventory Advisor", fontSize = 13.sp) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
            )
        }

        if (subTab == 0) {
            ExcelSyncScreen(viewModel = viewModel)
        } else {
            AiSuggestionsScreen(viewModel = viewModel)
        }
    }
}
