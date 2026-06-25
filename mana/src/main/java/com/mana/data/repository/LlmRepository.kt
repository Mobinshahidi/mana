package com.mana.data.repository

import android.util.Log
import com.mana.data.database.dao.ChatDao
import com.mana.data.database.entity.ChatMessage
import com.mana.data.database.entity.TransactionType
import com.mana.data.model.ChatContext
import com.mana.data.preferences.UserPreferencesRepository
import com.mana.domain.service.LlmService
import com.mana.utils.CurrencyUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmRepository @Inject constructor(
private val llmService: LlmService,
private val chatDao: ChatDao,
private val modelRepository: ModelRepository,
private val aiContextRepository: AiContextRepository,
private val userPreferencesRepository: UserPreferencesRepository
) {
fun getAllMessages(): Flow<List<ChatMessage>> = chatDao.getAllMessages()

fun getAllMessagesIncludingSystem(): Flow<List<ChatMessage>> = chatDao.getAllMessagesIncludingSystem()

suspend fun sendMessage(userMessage: String): Result<String> {
// Save user message
val userChatMessage = ChatMessage(
message = userMessage,
isUser = true
)
chatDao.insertMessage(userChatMessage)

// Initialize LLM if needed
if (!llmService.isInitialized()) {
val modelFile = modelRepository.getModelFile()
if (!modelFile.exists()) {
return Result.failure(Exception("Model not downloaded"))
}

val initResult = llmService.initialize(modelFile.absolutePath)
if (initResult.isFailure) {
return Result.failure(initResult.exceptionOrNull() ?: Exception("Failed to initialize LLM"))
}
}

// Generate response
val responseResult = llmService.generateResponse(userMessage)

return if (responseResult.isSuccess) {
val response = responseResult.getOrNull() ?: ""

// Save AI response
val aiChatMessage = ChatMessage(
message = response,
isUser = false
)
chatDao.insertMessage(aiChatMessage)

Result.success(response)
} else {
Result.failure(responseResult.exceptionOrNull() ?: Exception("Failed to generate response"))
}
}

fun sendMessageStream(userMessage: String): Flow<String> = flow {
// Check if this is the first message (no existing messages)
val existingMessages = chatDao.getAllMessagesForContext()
val isNewChat = existingMessages.isEmpty()

// If new chat, add system prompt first
if (isNewChat) {
// Try to get stored system prompt first
val storedPrompt = userPreferencesRepository.getSystemPrompt().first()
val systemPrompt = if (storedPrompt.isNullOrEmpty()) {
// Generate new prompt if none exists
val chatContext = aiContextRepository.getChatContext()
val newPrompt = buildSystemPrompt(chatContext)
// Save for future use
userPreferencesRepository.updateSystemPrompt(newPrompt)
newPrompt
} else {
storedPrompt
}

val systemMessage = ChatMessage(
message = systemPrompt,
isUser = false,
isSystemPrompt = true
)
chatDao.insertMessage(systemMessage)
Log.d("LlmRepository", "System prompt added to new chat")
}

// Check token limit before processing
val currentMessages = chatDao.getAllMessagesForContext()
val conversationText = buildConversationContext(currentMessages, userMessage)
val estimatedTokens = conversationText.length / 4 // Rough estimation

if (estimatedTokens > 1200) { // Leave some buffer (1200 out of 1280)
// Don't process, throw error to inform user
throw Exception("Chat memory is full. Please clear the chat to continue.")
}

// Save user message
val userChatMessage = ChatMessage(
message = userMessage,
isUser = true
)
Log.d("LlmRepository", "Saving user message: ${userMessage.take(50)}...")
chatDao.insertMessage(userChatMessage)
Log.d("LlmRepository", "User message saved")

// Check if model is downloading
val currentModelState = modelRepository.modelState.first()
if (currentModelState == ModelState.DOWNLOADING) {
throw Exception("Model is currently downloading. Please wait for download to complete.")
}

// Initialize LLM if needed
if (!llmService.isInitialized()) {
val modelFile = modelRepository.getModelFile()
if (!modelFile.exists()) {
throw Exception("Model not downloaded. Please download from Settings.")
}

val initResult = llmService.initialize(modelFile.absolutePath)
if (initResult.isFailure) {
throw initResult.exceptionOrNull() ?: Exception("Failed to initialize LLM")
}
}

// Get ALL conversation history (including system prompt)
val allMessages = chatDao.getAllMessagesForContext()
val conversationContext = buildConversationContext(allMessages, userMessage)

// Log the final message being sent to LLM
Log.d("LlmRepository", "=== SENDING TO LLM ===")
Log.d("LlmRepository", "Total messages in context: ${allMessages.size}")
Log.d("LlmRepository", "Context length: ${conversationContext.length} characters")
Log.d("LlmRepository", "Estimated tokens: ${conversationContext.length / 4}")
Log.d("LlmRepository", "=== FULL CONTEXT ===")
Log.d("LlmRepository", conversationContext)
Log.d("LlmRepository", "=== END CONTEXT ===")

// Stream response and accumulate for saving
val responseBuilder = StringBuilder()
var messageInserted = false

llmService.generateResponseStream(conversationContext)
.collect { partialResponse ->
responseBuilder.append(partialResponse)
emit(partialResponse)
}

// Save the complete AI response at the end
val finalResponse = responseBuilder.toString()
Log.d("LlmRepository", "Saving AI response: ${finalResponse.take(50)}...")
val aiMessage = ChatMessage(
message = finalResponse,
isUser = false
)
chatDao.insertMessage(aiMessage)
Log.d("LlmRepository", "AI response saved")
}

suspend fun deleteAllMessages() {
chatDao.deleteAllMessages()
}

suspend fun deleteOldMessages(beforeTimestamp: Long) {
chatDao.deleteMessagesBefore(beforeTimestamp)
}

suspend fun getMessageCount(): Int = chatDao.getMessageCount()

private suspend fun buildConversationContext(
history: List<ChatMessage>,
currentMessage: String
): String {
val contextBuilder = StringBuilder()
var systemPromptCount = 0
var userMessageCount = 0
var aiMessageCount = 0

// Add all conversation history (already in ASC order)
history.forEach { msg ->
when {
msg.isSystemPrompt -> {
systemPromptCount++
// System prompt is already formatted
contextBuilder.append(msg.message)
contextBuilder.append("\n\n")
}
msg.isUser -> {
userMessageCount++
contextBuilder.append("User: ${msg.message}\n")
}
else -> {
aiMessageCount++
contextBuilder.append("Assistant: ${msg.message}\n")
}
}
}

// Add current message (not yet in history)
contextBuilder.append("User: $currentMessage\n")
contextBuilder.append("Assistant:")

Log.d("LlmRepository", "Context composition: $systemPromptCount system prompts, $userMessageCount user messages, $aiMessageCount AI messages")

return contextBuilder.toString()
}

private fun buildSystemPrompt(context: ChatContext): String {
val monthSummary = context.monthSummary
val topCategories = context.topCategories
val activeSubs = context.activeSubscriptions
val stats = context.quickStats
val accountBalances = context.accountBalances

val totalSubAmount = activeSubs.sumOf { it.amount.toDouble() }.toBigDecimal()
val upcomingPayments = activeSubs.filter { it.nextPaymentDays <= 7 }

// Calculate total net worth from account balances
val totalCashBalance = accountBalances.filter { !it.isCreditCard }.sumOf { it.balance }
val totalCreditCardDebt = accountBalances.filter { it.isCreditCard }.sumOf { it.balance }
val netWorth = totalCashBalance - totalCreditCardDebt

return """
شما «مانا» هستید؛ یک دستیار مالی دوستانه که به کاربر کمک می‌کند هزینه‌ها را پیگیری کند و پولش را بهتر مدیریت کند.

نمای کلی مالی (${context.currentDate}):
- این ماه: ${CurrencyUtils.formatCurrency(monthSummary.totalExpense)} خرج، ${CurrencyUtils.formatCurrency(monthSummary.totalIncome)} درآمد
- ${monthSummary.transactionCount} تراکنش (روز ${monthSummary.currentDay}/${monthSummary.daysInMonth})
- میانگین روزانه: ${CurrencyUtils.formatCurrency(stats.avgDailySpending)}
- خالص دارایی (مانه): ${CurrencyUtils.formatCurrency(netWorth)} (${CurrencyUtils.formatCurrency(totalCashBalance)} در حساب‌ها - ${CurrencyUtils.formatCurrency(totalCreditCardDebt)} بدهی کارت اعتباری)

موجودی حساب‌ها:
${if (accountBalances.isNotEmpty()) {
accountBalances.joinToString("\n") { balance ->
val accountType = if (balance.isCreditCard) "کارت اعتباری" else "حساب"
val balanceSign = if (balance.isCreditCard) "-" else ""
"- ${balance.bankName} $accountType ${balance.accountLast4?.let { "**$it" } ?: ""}: ${balanceSign}${CurrencyUtils.formatCurrency(balance.balance)} ${balance.currency}"
}
} else "هیچ موجودی ثبت نشده است"}

دسته‌بندی‌های پرتکرار هزینه:
${topCategories.joinToString("\n") { "- ${it.category}: ${CurrencyUtils.formatCurrency(it.amount)} (${it.percentage.toInt()}%)" }}

اشتراک‌های فعال: ${activeSubs.size} سرویس (${CurrencyUtils.formatCurrency(totalSubAmount)} در ماه)
${if (upcomingPayments.isNotEmpty()) "⚠️ ${upcomingPayments.size} پرداخت در ۷ روز آینده" else ""}

تراکنش‌های اخیر (۱۴ روز گذشته):
${context.recentTransactions.take(10).joinToString("\n") { transaction ->
val dateStr = transaction.dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a"))
val typeStr = if (transaction.transactionType == TransactionType.INCOME) "+" else "-"
"- $dateStr: ${transaction.merchantName} ${typeStr}${CurrencyUtils.formatCurrency(transaction.amount)} (${transaction.category})"
}}

${if (stats.mostFrequentMerchant != null) "بیشترین مراجعه: ${stats.mostFrequentMerchant} (${stats.mostFrequentMerchantCount} بار)" else ""}

راهنما:
- پاسخ‌ها را به فارسی و با لحن دوستانه بده
- در مورد خرج‌کردها قضاوت نکن و راهکار عملی بده
- از واحد پول واقعی کاربر استفاده کن (IRR، USD و ...)
- فقط متن ساده استفاده کن؛ از مارک‌داون استفاده نکن
- از علامت‌های * _ ` و قالب‌بندی‌های خاص استفاده نکن
- کوتاه، روشن و قابل فهم بنویس
- برای فهرست‌ها از خط تیره یا شماره استفاده کن
""".trimIndent()
}

suspend fun updateSystemPrompt() {
val chatContext = aiContextRepository.getChatContext()
val newPrompt = buildSystemPrompt(chatContext)
userPreferencesRepository.updateSystemPrompt(newPrompt)
Log.d("LlmRepository", "System prompt updated with latest financial data")
}

suspend fun getFormattedContextForDisplay(): String {
val chatContext = aiContextRepository.getChatContext()
val monthSummary = chatContext.monthSummary
val recentCount = minOf(chatContext.recentTransactions.size, 10)
val activeSubs = chatContext.activeSubscriptions

return """
سلام! من مانا هستم، دستیار مالی شما.

من به این اطلاعات دسترسی دارم:
• تراکنش‌های ۱۴ روز گذشته شما ($recentCount مورد اخیر)
• خلاصه این ماه (${monthSummary.transactionCount} تراکنش)
• درآمد و هزینه‌های ماهانه
• دسته‌بندی‌های پرتکرار هزینه
• اشتراک‌های فعال (${activeSubs.size} سرویس)
• میانگین خرج روزانه

می‌توانم کمک کنم خرج‌ها را بهتر بفهمید، راه‌های صرفه‌جویی پیدا کنید و درباره وضعیت مالی اخیرتان بپرسید.

دوست دارید چه چیزی بدانید؟
""".trimIndent()
}
}
