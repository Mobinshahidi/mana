package com.mana.parser.core.rule

data class ParserConfig(
    val version: Int = 1,
    val banks: List<BankConfig>
)

data class BankConfig(
    val name: String,
    val senders: List<String>,
    val currency: String = "IRR",
    val amountPatterns: List<ExtractionRule>,
    val typeDetection: TypeDetectionConfig,
    val merchantMap: Map<String, String> = emptyMap(),
    val balancePatterns: List<ExtractionRule> = emptyList(),
    val accountPatterns: List<ExtractionRule> = emptyList(),
    val referencePatterns: List<ExtractionRule> = emptyList(),
    val transactionIndicators: List<String> = emptyList(),
    val excludeKeywords: List<String> = emptyList()
)

data class TypeDetectionConfig(
    val keywords: Map<String, String> = emptyMap(),
    val signPosition: String? = null
)

data class ExtractionRule(
    val regex: String,
    val group: Int = 1
)

data class ParseResult(
    val bankName: String,
    val amount: String?,
    val type: String?,
    val merchant: String?,
    val account: String?,
    val balance: String?,
    val reference: String?,
    val confidence: Float,
    val rawMessage: String,
    val detectedBySender: Boolean = false
)
