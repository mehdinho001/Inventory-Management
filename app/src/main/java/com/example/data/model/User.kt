package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    TECHNICIEN("Technicien", 0xFF0284C7),
    MANAGER("Manager", 0xFF16A34A),
    ADMIN("Admin", 0xFF7C3AED);

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.trim().lowercase()) {
                "admin" -> ADMIN
                "manager" -> MANAGER
                else -> TECHNICIEN
            }
        }
    }
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String,
    val fullName: String,
    val role: UserRole,
    val department: String = "Maintenance & Warehouse",
    val createdAt: Long = System.currentTimeMillis()
)
