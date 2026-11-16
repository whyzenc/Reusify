package com.example.data

import android.content.Context
import com.example.data.model.UserRole

data class PendingRegistrationInfo(
  val username: String = "",
  val phone: String = "",
)

object RolePreferenceStore {
  private const val PREFS_NAME = "reusify_role_prefs"
  private const val KEY_PENDING_ROLE = "pending_login_role"
  private const val KEY_PENDING_USERNAME = "pending_username"
  private const val KEY_PENDING_PHONE = "pending_phone"
  private const val KEY_PENDING_ADMIN_LOGIN = "pending_admin_login"
  private const val KEY_USER_ROLE_PREFIX = "user_role_"

  fun savePendingRole(context: Context, role: UserRole) {
    context.applicationContext
      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_PENDING_ROLE, role.code)
      .apply()
  }

  fun getPendingRole(context: Context): UserRole {
    val raw =
      context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_PENDING_ROLE, null)
    return UserRole.fromCode(raw)
  }

  fun savePendingAdminIntent(context: Context, isAdminLogin: Boolean) {
    context.applicationContext
      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putBoolean(KEY_PENDING_ADMIN_LOGIN, isAdminLogin)
      .apply()
  }

  fun getPendingAdminIntent(context: Context): Boolean =
    context.applicationContext
      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getBoolean(KEY_PENDING_ADMIN_LOGIN, false)

  fun consumePendingAdminIntent(context: Context): Boolean {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val flag = prefs.getBoolean(KEY_PENDING_ADMIN_LOGIN, false)
    if (flag) {
      prefs.edit().remove(KEY_PENDING_ADMIN_LOGIN).apply()
    }
    return flag
  }

  fun savePendingRegistration(context: Context, username: String, phone: String) {
    context.applicationContext
      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_PENDING_USERNAME, username.trim())
      .putString(KEY_PENDING_PHONE, phone.trim())
      .apply()
  }

  fun consumePendingRegistration(context: Context): PendingRegistrationInfo {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val username = prefs.getString(KEY_PENDING_USERNAME, "").orEmpty()
    val phone = prefs.getString(KEY_PENDING_PHONE, "").orEmpty()
    if (username.isNotBlank() || phone.isNotBlank()) {
      prefs.edit()
        .remove(KEY_PENDING_USERNAME)
        .remove(KEY_PENDING_PHONE)
        .apply()
    }
    return PendingRegistrationInfo(username = username, phone = phone)
  }

  fun resolveInitialRoleForUser(context: Context, userId: String): UserRole {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val pending = prefs.getString(KEY_PENDING_ROLE, null)
    if (pending != null) {
      val resolved = UserRole.fromCode(pending)
      prefs.edit()
        .putString("$KEY_USER_ROLE_PREFIX$userId", resolved.code)
        .remove(KEY_PENDING_ROLE)
        .apply()
      return resolved
    }
    val saved = prefs.getString("$KEY_USER_ROLE_PREFIX$userId", null)
    return UserRole.fromCode(saved)
  }

  fun saveActiveRole(context: Context, userId: String, role: UserRole) {
    context.applicationContext
      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString("$KEY_USER_ROLE_PREFIX$userId", role.code)
      .apply()
  }

  private fun chatReadPrefsName(userId: String): String = "reusify_chat_read_$userId"

  fun getChatReadTimestamps(context: Context, userId: String): Map<String, Long> {
    if (userId.isBlank()) return emptyMap()
    val prefs =
      context.applicationContext.getSharedPreferences(chatReadPrefsName(userId), Context.MODE_PRIVATE)
    return prefs.all.mapNotNull { (key, value) ->
      (value as? Long)?.let { key to it }
    }.toMap()
  }

  fun markChatThreadRead(
    context: Context,
    userId: String,
    threadKey: String,
    readMillis: Long,
  ) {
    if (userId.isBlank() || threadKey.isBlank()) return
    context.applicationContext
      .getSharedPreferences(chatReadPrefsName(userId), Context.MODE_PRIVATE)
      .edit()
      .putLong(threadKey, readMillis)
      .apply()
  }

  fun markAllChatThreadsRead(
    context: Context,
    userId: String,
    readTimestamps: Map<String, Long>,
  ) {
    if (userId.isBlank() || readTimestamps.isEmpty()) return
    val editor =
      context.applicationContext
        .getSharedPreferences(chatReadPrefsName(userId), Context.MODE_PRIVATE)
        .edit()
    readTimestamps.forEach { (key, millis) -> editor.putLong(key, millis) }
    editor.apply()
  }
}
