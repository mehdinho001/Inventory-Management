package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.IndustrialCyan
import com.example.ui.theme.RoleAdminPurple
import com.example.ui.theme.RoleManagerGreen
import com.example.ui.theme.RoleTechSky
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StockDanger

@Composable
fun AuthDialog(
    currentUser: User?,
    allUsers: List<User>,
    onDismiss: () -> Unit,
    onSelectUser: (User) -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, UserRole) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Switch Account, 1: Register New, 2: Login

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }

    // Register Form State
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.TECHNICIEN) }
    var formError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = IndustrialCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "User Accounts & Roles",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs: Switch / Register / Login
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate850,
                    contentColor = IndustrialCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; formError = null },
                        text = { Text("Accounts", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; formError = null },
                        text = { Text("Register", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; formError = null },
                        text = { Text("Sign In", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> {
                        // Switch between registered users
                        Text(
                            text = "SELECT ACTIVE OPERATOR ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        allUsers.forEach { u ->
                            val isCurrent = currentUser?.email == u.email
                            val roleColor = when (u.role) {
                                UserRole.ADMIN -> RoleAdminPurple
                                UserRole.MANAGER -> RoleManagerGreen
                                UserRole.TECHNICIEN -> RoleTechSky
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelectUser(u)
                                        onDismiss()
                                    }
                                    .testTag("account_item_${u.role.name.lowercase()}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) Slate800 else Slate850
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isCurrent) roleColor else Slate700
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = u.fullName,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = u.email,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    Surface(
                                        color = roleColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, roleColor)
                                    ) {
                                        Text(
                                            text = u.role.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = roleColor
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register New Staff Member")
                        }
                    }

                    1 -> {
                        // Register Form
                        Text(
                            text = "REGISTER NEW STAFF ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Jean Dupont") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("staff@inventory.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regPass,
                            onValueChange = { regPass = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_pass_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Role Selector
                        Text(
                            text = "Access Role (Permissions & Metrics):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserRole.values().forEach { r ->
                                val isSelected = regRole == r
                                val color = when (r) {
                                    UserRole.ADMIN -> RoleAdminPurple
                                    UserRole.MANAGER -> RoleManagerGreen
                                    UserRole.TECHNICIEN -> RoleTechSky
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) color else Slate800,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { regRole = r }
                                        .testTag("select_reg_role_${r.name.lowercase()}")
                                ) {
                                    Text(
                                        text = r.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Slate950 else Color.White
                                        ),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        formError?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = StockDanger, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regEmail.isBlank() || regPass.isBlank()) {
                                    formError = "Please fill in all registration fields"
                                    return@Button
                                }
                                onRegister(regEmail.trim(), regPass.trim(), regFullName.trim(), regRole)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_submit_register"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan)
                        ) {
                            Text("Create Staff Account", fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // Login Form
                        Text(
                            text = "SIGN IN TO YOUR ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Email") },
                            placeholder = { Text("admin@inventory.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = loginPass,
                            onValueChange = { loginPass = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_pass_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialCyan,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        formError?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = StockDanger, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (loginEmail.isBlank() || loginPass.isBlank()) {
                                    formError = "Enter email and password"
                                    return@Button
                                }
                                onLogin(loginEmail.trim(), loginPass.trim())
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_submit_login"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialCyan)
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
