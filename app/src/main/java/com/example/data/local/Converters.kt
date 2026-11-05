package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TransactionType
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (_: Exception) {
            TransactionType.MANUAL_ADJUST
        }
    }

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return try {
            UserRole.valueOf(value)
        } catch (_: Exception) {
            UserRole.TECHNICIEN
        }
    }
}
