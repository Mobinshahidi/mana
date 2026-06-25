package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import io.ktor.server.plugins.cors.routing.*
import com.mana.parser.core.bank.BankParserRegistry
import com.mana.parser.core.bank.*
import com.mana.parser.core.rule.SmartParser
import com.mana.parser.core.rule.SmartParseResult
import com.example.ui.ParseViews.respondParsePage
import com.example.ui.ParseViews.respondSmartParsePage
import com.example.ui.ParseViews.renderParseResult
import com.example.ui.ParseViews.renderSmartParseResult

fun Application.configureRouting() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    val registry = BankParserRegistry(
        listOf(
            MelliBankParser(), KeshavarziBankParser(), ParsianBankParser(),
            ResalatBankParser(), RefahBankParser(), BluBankParser()
        )
    )

    routing {
        // Serve static resources
        staticResources("/static", "static")

        // Serve HTMX page at root
        get("/") { call.respondParsePage() }
        get("/tools/parse") { call.respondParsePage() }

        // Smart Parse - auto-detect bank and message type (no sender required)
        get("/tools/smart-parse") { call.respondSmartParsePage() }

        // HTMX endpoint that returns an HTML snippet with parsed result
        post("/htmx/parse") {
            val params = call.receiveParameters()
            val sender = params["sender"]?.trim().orEmpty()
            val smsBody = params["smsBody"]?.trim().orEmpty()
            val tsStr = params["timestamp"]?.trim()
            val ts = tsStr?.toLongOrNull() ?: System.currentTimeMillis()

            if (sender.isEmpty() || smsBody.isEmpty()) {
                call.respondHtml(HttpStatusCode.BadRequest) {
                    body {
                        div {
                            p { +"Sender and message body are required." }
                        }
                    }
                }
                return@post
            }

            val parser = registry.getParser(sender)
            val parsed = parser?.parse(smsBody, sender, ts)

            call.respondHtml(HttpStatusCode.OK) { body { renderParseResult(parsed, sender, smsBody) } }
        }

        // Smart Parse endpoint - auto-detect bank and parse any Iranian SMS
        post("/htmx/smart-parse") {
            val params = call.receiveParameters()
            val sender = params["sender"]?.trim()?.takeIf { it.isNotEmpty() }
            val smsBody = params["smsBody"]?.trim().orEmpty()

            if (smsBody.isEmpty()) {
                call.respondHtml(HttpStatusCode.BadRequest) {
                    body {
                        div {
                            p { +"Message body is required." }
                        }
                    }
                }
                return@post
            }

            val result = SmartParser.parse(smsBody, sender)
            val messageType = SmartParser.classifyMessageType(smsBody)

            call.respondHtml(HttpStatusCode.OK) { body { renderSmartParseResult(result, messageType, smsBody) } }
        }
    }
}
