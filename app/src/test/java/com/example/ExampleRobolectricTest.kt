package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.repository.BuyJumpRepositoryImpl

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BuyJump", appName)
  }

  @Test
  fun `repository initialization has seed data`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BuyJumpRepositoryImpl.getInstance(context)
    assertNotNull(repo.categories.value)
    assertNotNull(repo.products.value)
    assertNotNull(repo.sellers.value)
  }
}
