package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MarketItem
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.util.CustomClassMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    assertEquals("Reusify Padang", appName)
  }

  @Test
  fun `firestore boolean properties deserialize with PropertyName`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    val userRef = db.collection("users").document("u1")
    val profile =
      CustomClassMapper.convertToCustomClass(
        mapOf(
          "userId" to "u1",
          "displayName" to "Test Mahasiswa",
          "isPremium" to true,
          "isVerifiedStudent" to false,
        ),
        UserProfile::class.java,
        userRef,
      )
    assertTrue(profile.isPremium)
    assertFalse(profile.isVerifiedStudent)

    val itemRef = db.collection("items").document("i1")
    val item =
      CustomClassMapper.convertToCustomClass(
        mapOf(
          "sellerId" to "u1",
          "title" to "Buku Bekas",
          "isFeatured" to true,
        ),
        MarketItem::class.java,
        itemRef,
      )
    assertTrue(item.isFeatured)
  }
}
