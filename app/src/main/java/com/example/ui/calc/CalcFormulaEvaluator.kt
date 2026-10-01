package com.example.ui.calc

import com.example.data.model.CalcCell
import com.example.data.model.CellFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CalcFormulaEvaluator {

    private val symbols = DecimalFormatSymbols(Locale.FRANCE).apply {
        decimalSeparator = ','
        groupingSeparator = ' '
    }
    private val currencyFormat = DecimalFormat("#,##0.00 €", symbols)
    private val percentFormat = DecimalFormat("#,##0.0 %", symbols)
    private val numberFormat = DecimalFormat("#,##0.##", symbols)

    fun evaluate(
        coord: String,
        cell: CalcCell,
        allCells: Map<String, CalcCell>,
        visited: MutableSet<String> = mutableSetOf()
    ): String {
        val raw = cell.rawValue.trim()
        if (!raw.startsWith("=")) {
            return formatDisplay(raw, cell.format)
        }

        if (visited.contains(coord)) {
            return "#CIRCULAIRE!"
        }
        visited.add(coord)

        val formula = raw.substring(1).trim()
        val result = try {
            evalExpression(formula, allCells, visited)
        } catch (e: Exception) {
            "#ERREUR!"
        }
        visited.remove(coord)
        return formatDisplay(result, cell.format)
    }

    private fun evalExpression(
        formula: String,
        allCells: Map<String, CalcCell>,
        visited: MutableSet<String>
    ): String {
        val upper = formula.uppercase()

        // 1. Functions
        if (upper.startsWith("SUM(") && upper.endsWith(")")) {
            val inner = formula.substring(4, formula.length - 1)
            val values = resolveNumbers(inner, allCells, visited)
            return values.sum().toString()
        }
        if ((upper.startsWith("AVERAGE(") || upper.startsWith("AVG(")) && upper.endsWith(")")) {
            val paren = formula.indexOf("(")
            val inner = formula.substring(paren + 1, formula.length - 1)
            val values = resolveNumbers(inner, allCells, visited)
            return if (values.isNotEmpty()) (values.sum() / values.size).toString() else "0"
        }
        if (upper.startsWith("COUNT(") && upper.endsWith(")")) {
            val inner = formula.substring(6, formula.length - 1)
            val values = resolveNumbers(inner, allCells, visited)
            return values.size.toString()
        }
        if (upper.startsWith("MIN(") && upper.endsWith(")")) {
            val inner = formula.substring(4, formula.length - 1)
            val values = resolveNumbers(inner, allCells, visited)
            return (values.minOrNull() ?: 0.0).toString()
        }
        if (upper.startsWith("MAX(") && upper.endsWith(")")) {
            val inner = formula.substring(4, formula.length - 1)
            val values = resolveNumbers(inner, allCells, visited)
            return (values.maxOrNull() ?: 0.0).toString()
        }
        if (upper.startsWith("PRODUCT(") && upper.endsWith(")")) {
            val inner = formula.substring(8, formula.length - 1)
            val values = resolveNumbers(inner, allCells, visited)
            var p = 1.0
            values.forEach { p *= it }
            return p.toString()
        }
        if (upper.startsWith("IF(") && upper.endsWith(")")) {
            val inner = formula.substring(3, formula.length - 1)
            val parts = splitArgs(inner)
            if (parts.size >= 2) {
                val cond = evalCondition(parts[0], allCells, visited)
                return if (cond) {
                    evalExpression(parts[1], allCells, visited)
                } else if (parts.size >= 3) {
                    evalExpression(parts[2], allCells, visited)
                } else "0"
            }
        }

        // 2. Simple arithmetic (+, -, *, /)
        if (formula.contains("+")) {
            val tokens = formula.split("+")
            val sum = tokens.sumOf { getNumericValue(it.trim(), allCells, visited) }
            return sum.toString()
        }
        if (formula.contains("-") && !formula.startsWith("-")) {
            val tokens = formula.split("-")
            val first = getNumericValue(tokens[0].trim(), allCells, visited)
            val rest = tokens.drop(1).sumOf { getNumericValue(it.trim(), allCells, visited) }
            return (first - rest).toString()
        }
        if (formula.contains("*")) {
            val tokens = formula.split("*")
            var prod = 1.0
            tokens.forEach { prod *= getNumericValue(it.trim(), allCells, visited) }
            return prod.toString()
        }
        if (formula.contains("/")) {
            val tokens = formula.split("/")
            val num = getNumericValue(tokens[0].trim(), allCells, visited)
            val den = getNumericValue(tokens[1].trim(), allCells, visited)
            return if (den != 0.0) (num / den).toString() else "#DIV/0!"
        }

        // 3. Single cell reference (e.g. A1) or number
        if (isCellCoord(formula.trim())) {
            val target = allCells[formula.trim().uppercase()]
            return if (target != null) evaluate(formula.trim().uppercase(), target, allCells, visited) else "0"
        }

        return formula
    }

    private fun evalCondition(
        condStr: String,
        allCells: Map<String, CalcCell>,
        visited: MutableSet<String>
    ): Boolean {
        val ops = listOf(">=", "<=", "!=", "=", ">", "<")
        for (op in ops) {
            if (condStr.contains(op)) {
                val leftStr = condStr.substringBefore(op).trim()
                val rightStr = condStr.substringAfter(op).trim()
                val leftVal = getNumericValue(leftStr, allCells, visited)
                val rightVal = getNumericValue(rightStr, allCells, visited)
                return when (op) {
                    ">" -> leftVal > rightVal
                    "<" -> leftVal < rightVal
                    ">=" -> leftVal >= rightVal
                    "<=" -> leftVal <= rightVal
                    "=" -> leftVal == rightVal
                    "!=" -> leftVal != rightVal
                    else -> false
                }
            }
        }
        return false
    }

    private fun resolveNumbers(
        argsStr: String,
        allCells: Map<String, CalcCell>,
        visited: MutableSet<String>
    ): List<Double> {
        val list = mutableListOf<Double>()
        val parts = splitArgs(argsStr)
        for (p in parts) {
            val arg = p.trim().uppercase()
            if (arg.contains(":")) {
                // Range like A1:A5
                val rangeCells = expandRange(arg)
                for (c in rangeCells) {
                    val cell = allCells[c]
                    if (cell != null) {
                        val evaluated = evaluate(c, cell, allCells, visited)
                        parseNumber(evaluated)?.let { list.add(it) }
                    }
                }
            } else {
                list.add(getNumericValue(arg, allCells, visited))
            }
        }
        return list
    }

    private fun getNumericValue(
        token: String,
        allCells: Map<String, CalcCell>,
        visited: MutableSet<String>
    ): Double {
        val clean = token.trim().uppercase()
        if (isCellCoord(clean)) {
            val c = allCells[clean]
            if (c != null) {
                val eval = evaluate(clean, c, allCells, visited)
                return parseNumber(eval) ?: 0.0
            }
            return 0.0
        }
        return parseNumber(clean) ?: 0.0
    }

    private fun splitArgs(args: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        val sb = StringBuilder()
        for (char in args) {
            when (char) {
                '(', '[' -> depth++
                ')', ']' -> depth--
                ',', ';' -> {
                    if (depth == 0) {
                        result.add(sb.toString())
                        sb.clear()
                        continue
                    }
                }
            }
            sb.append(char)
        }
        if (sb.isNotEmpty()) result.add(sb.toString())
        return result
    }

    fun expandRange(range: String): List<String> {
        val parts = range.split(":")
        if (parts.size != 2) return listOf(range)
        val start = parts[0].trim().uppercase()
        val end = parts[1].trim().uppercase()

        val startCol = start.filter { it.isLetter() }
        val startRow = start.filter { it.isDigit() }.toIntOrNull() ?: 1
        val endCol = end.filter { it.isLetter() }
        val endRow = end.filter { it.isDigit() }.toIntOrNull() ?: 1

        val col1 = colLetterToIndex(startCol)
        val col2 = colLetterToIndex(endCol)
        val minCol = minOf(col1, col2)
        val maxCol = maxOf(col1, col2)
        val minRow = minOf(startRow, endRow)
        val maxRow = maxOf(startRow, endRow)

        val result = mutableListOf<String>()
        for (c in minCol..maxCol) {
            val letter = indexToColLetter(c)
            for (r in minRow..maxRow) {
                result.add("$letter$r")
            }
        }
        return result
    }

    fun isCellCoord(str: String): Boolean {
        val letters = str.filter { it.isLetter() }
        val digits = str.filter { it.isDigit() }
        return letters.isNotEmpty() && digits.isNotEmpty() && letters.length + digits.length == str.length
    }

    fun colLetterToIndex(letter: String): Int {
        var res = 0
        for (ch in letter.uppercase()) {
            res = res * 26 + (ch - 'A' + 1)
        }
        return res - 1
    }

    fun indexToColLetter(index: Int): String {
        var num = index
        val sb = StringBuilder()
        while (num >= 0) {
            sb.append(('A'.code + (num % 26)).toChar())
            num = num / 26 - 1
        }
        return sb.reverse().toString()
    }

    fun parseNumber(str: String): Double? {
        val clean = str.replace("€", "")
            .replace("$", "")
            .replace("%", "")
            .replace(" ", "")
            .replace(",", ".")
            .trim()
        return clean.toDoubleOrNull()
    }

    fun formatDisplay(raw: String, format: CellFormat): String {
        val num = parseNumber(raw) ?: return raw
        return when (format) {
            CellFormat.GENERAL -> {
                if (num == num.toLong().toDouble()) num.toLong().toString() else "%.2f".format(Locale.FRANCE, num)
            }
            CellFormat.NUMBER -> numberFormat.format(num)
            CellFormat.CURRENCY_EUR -> currencyFormat.format(num)
            CellFormat.CURRENCY_USD -> "$%.2f".format(Locale.US, num)
            CellFormat.PERCENT -> percentFormat.format(num / (if (raw.contains("%")) 100.0 else 1.0))
            CellFormat.DATE -> raw
        }
    }
}
