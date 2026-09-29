package com.sanad.app410924

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sanad.app410924.databinding.ActivityMainBinding
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var inputExpression = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9, binding.btnDot
        )

        for (btn in numberButtons) {
            btn.setOnClickListener {
                inputExpression.append((it as com.google.android.material.button.MaterialButton).text)
                binding.tvResult.text = inputExpression.toString()
            }
        }

        binding.btnPlus.setOnClickListener { appendOperator("+") }
        binding.btnMinus.setOnClickListener { appendOperator("-") }
        binding.btnMultiply.setOnClickListener { appendOperator("×") }
        binding.btnDivide.setOnClickListener { appendOperator("÷") }
        binding.btnBracketOpen.setOnClickListener { appendToInput("(") }
        binding.btnBracketClose.setOnClickListener { appendToInput(")") }

        binding.btnSin.setOnClickListener { appendFunction("sin(") }
        binding.btnCos.setOnClickListener { appendFunction("cos(") }
        binding.btnTan.setOnClickListener { appendFunction("tan(") }
        binding.btnSqrt.setOnClickListener { appendFunction("√(") }

        binding.btnClear.setOnClickListener {
            inputExpression.clear()
            binding.tvResult.text = "0"
            binding.tvHistory.text = ""
        }

        binding.btnEquals.setOnClickListener {
            try {
                val expr = inputExpression.toString()
                if (expr.isNotEmpty()) {
                    val result = evaluateExpression(expr)
                    binding.tvHistory.text = expr
                    binding.tvResult.text = formatResult(result)
                    inputExpression.clear()
                    inputExpression.append(formatResult(result))
                }
            } catch (e: Exception) {
                binding.tvResult.text = "خطأ"
            }
        }
    }

    private fun appendOperator(op: String) {
        if (inputExpression.isNotEmpty()) {
            inputExpression.append(" $op ")
            binding.tvResult.text = inputExpression.toString()
        }
    }

    private fun appendFunction(func: String) {
        inputExpression.append(func)
        binding.tvResult.text = inputExpression.toString()
    }

    private fun appendToInput(str: String) {
        inputExpression.append(str)
        binding.tvResult.text = inputExpression.toString()
    }

    private fun evaluateExpression(str: String): Double {
        val sanitized = str.replace("×", "*").replace("÷", "/")
        return SimpleParser(sanitized).parse()
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    // محلل رياضي بسيط وبدائي يدعم الأولويات والدوال المثلثية والجذر
    private inner class SimpleParser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) x /= parseFactor() // division
                else return x
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "√" -> sqrt(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }
}