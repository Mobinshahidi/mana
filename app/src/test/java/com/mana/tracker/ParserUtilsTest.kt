package com.mana.tracker

import com.mana.tracker.data.parser.ParserUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ParserUtilsTest {

    @Test
    fun parseAmountPersianDigits() {
        val body = "برداشت از فروشگاه ایکس مبلغ ۵۰۰٬۰۰۰ ریال"
        val amount = ParserUtils.parseAmount(body)
        assertNotNull(amount)
        assertEquals(500000L, amount)
    }
}
