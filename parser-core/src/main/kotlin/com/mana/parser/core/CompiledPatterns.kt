package com.mana.parser.core

object CompiledPatterns {
    object Amount {
        val RIAL_PATTERN = Regex("""﷼\s*([0-9,]+(?:\.\d{2})?)""")
        val IRR_PATTERN = Regex("""IRR\s*([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(RIAL_PATTERN, IRR_PATTERN)
    }

    object Reference {
        val GENERIC_REF = Regex(
            """(?:Ref|Reference|Txn|Transaction)(?:\s+No)?[:\s]+([A-Z0-9]+)""",
            RegexOption.IGNORE_CASE
        )
        val ALL_PATTERNS = listOf(GENERIC_REF)
    }

    object Account {
        val AC_WITH_MASK = Regex(
            """(?:A/c|Account|Acct)(?:\s+No)?\.?\s+(?:XX+|\*+)?(\d{3,4})""",
            RegexOption.IGNORE_CASE
        )
        val ALL_PATTERNS = listOf(AC_WITH_MASK)
    }

    object Balance {
        val AVL_BAL_NO_CURRENCY = Regex("""(?:Bal|Balance|Avl Bal|Available Balance)[:\s]+([0-9,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(AVL_BAL_NO_CURRENCY)
    }

    object Merchant {
        val TO_PATTERN =
            Regex("""to\s+([^\.\n]+?)(?:\s+on|\s+at|\s+Ref)""", RegexOption.IGNORE_CASE)
        val FROM_PATTERN =
            Regex("""from\s+([^\.\n]+?)(?:\s+on|\s+at|\s+Ref)""", RegexOption.IGNORE_CASE)
        val AT_PATTERN = Regex("""at\s+([^\.\n]+?)(?:\s+on|\s+Ref)""", RegexOption.IGNORE_CASE)
        val FOR_PATTERN =
            Regex("""for\s+([^\.\n]+?)(?:\s+on|\s+at|\s+Ref)""", RegexOption.IGNORE_CASE)
        val ALL_PATTERNS = listOf(TO_PATTERN, FROM_PATTERN, AT_PATTERN, FOR_PATTERN)
    }

    object Cleaning {
        val TRAILING_PARENTHESES = Regex("""\s*\(.*?\)\s*$""")
        val REF_NUMBER_SUFFIX = Regex("""\s+Ref\s+No.*""", RegexOption.IGNORE_CASE)
        val DATE_SUFFIX = Regex("""\s+on\s+\d{2}.*""")
        val TIME_SUFFIX = Regex("""\s+at\s+\d{2}:\d{2}.*""")
        val TRAILING_DASH = Regex("""\s*-\s*$""")
    }

    object Currency {
        val ISO_CODE = Regex("""[A-Z]{3}""")
        val SPECIFIC_ISO = { code: String -> Regex(code, RegexOption.IGNORE_CASE) }
        val COMMON_CURRENCIES = Regex("""(?:IRR|﷼)""")
    }

    object Date {
        // dd/MM/yy e.g. 20/10/25
        val DD_MM_YY = Regex("""\d{1,2}/\d{1,2}/\d{2}""")

        // dd/MM/yyyy e.g. 20/10/2025
        val DD_MM_YYYY = Regex("""\d{1,2}/\d{1,2}/\d{4}""")

        // dd-MMM-yy e.g. 20-OCT-25
        val DD_MMM_YY = Regex("""\d{1,2}-[A-Za-z]{3}-\d{2}""", RegexOption.IGNORE_CASE)

        // dd-MM-yyyy e.g. 20-10-2025
        val DD_MM_YYYY_DASH = Regex("""\d{1,2}-\d{1,2}-\d{4}""")
    }

    object Time {
        // HH:mm:ss
        val HH_MM_SS = Regex("""\d{1,2}:\d{2}:\d{2}""")

        // HH:mm
        val HH_MM = Regex("""\d{1,2}:\d{2}""")
    }
}


