package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StarOffice", appName)
  }

  @Test
  fun `test formula evaluation`() {
    val cells = mapOf(
      "A1" to com.example.data.model.CalcCell(rawValue = "10"),
      "A2" to com.example.data.model.CalcCell(rawValue = "20"),
      "A3" to com.example.data.model.CalcCell(rawValue = "=SUM(A1:A2)")
    )
    val result = com.example.ui.calc.CalcFormulaEvaluator.evaluate("A3", cells["A3"]!!, cells)
    assertEquals("30", result)
  }
}
