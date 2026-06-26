package com.mana.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "sms_templates",
    foreignKeys = [
        ForeignKey(
            entity = UserBankEntity::class,
            parentColumns = ["id"],
            childColumns = ["bank_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bank_id")]
)
data class SmsTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "bank_id")
    val bankId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "transaction_type")
    val transactionType: String? = null,

    @ColumnInfo(name = "type_keywords")
    val typeKeywords: String? = null,

    @ColumnInfo(name = "amount_regex")
    val amountRegex: String? = null,

    @ColumnInfo(name = "balance_regex")
    val balanceRegex: String? = null,

    @ColumnInfo(name = "account_regex")
    val accountRegex: String? = null,

    @ColumnInfo(name = "merchant_regex")
    val merchantRegex: String? = null,

    @ColumnInfo(name = "reference_regex")
    val referenceRegex: String? = null,

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
