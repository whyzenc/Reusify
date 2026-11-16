package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.data.RolePreferenceStore
import com.example.data.model.ItemStatus
import com.example.data.model.MarketItem
import com.example.data.model.MarketTransaction
import com.example.data.model.TransactionStatus
import com.example.data.model.buildChatThreadKey
import com.example.data.repository.ReusifyRepository
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.MyListingsScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QrPaymentScreen
import com.example.ui.screens.SellItemScreen
import com.example.ui.theme.ReusifyTheme
import com.example.ui.viewmodel.ReusifyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ReusifyTheme {
        ReusifyRoot()
      }
    }
  }
}

@Composable
fun ReusifyRoot() {
  val currentUser by
    Firebase.auth
      .authStateFlow()
      .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

  val user = currentUser
  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    key(user.uid) {
      AuthenticatedReusifyApp(user = user)
    }
  }
}

@Composable
private fun AuthenticatedReusifyApp(user: FirebaseUser) {
  val appContext = LocalContext.current.applicationContext
  val initialRole = remember(user.uid) { RolePreferenceStore.resolveInitialRoleForUser(appContext, user.uid) }
  val pendingReg = remember(user.uid) { RolePreferenceStore.consumePendingRegistration(appContext) }
  val resolvedName =
    pendingReg.username.ifBlank { user.displayName.orEmpty() }.ifBlank { "Mahasiswa Padang" }
  val viewModel: ReusifyViewModel =
    viewModel(
      key = user.uid,
      factory =
        viewModelFactory {
          initializer {
            ReusifyViewModel(
              repository = ReusifyRepository(appContext),
              currentUserId = user.uid,
              currentUserName = resolvedName,
              currentUserEmail = user.email.orEmpty(),
              isEmailVerified = user.isEmailVerified,
              initialRole = initialRole,
              initialPhone = pendingReg.phone,
              onPersistRole = { role -> RolePreferenceStore.saveActiveRole(appContext, user.uid, role) },
              initialChatReadMap = RolePreferenceStore.getChatReadTimestamps(appContext, user.uid),
              onPersistChatRead = { threadKey, readMillis ->
                RolePreferenceStore.markChatThreadRead(appContext, user.uid, threadKey, readMillis)
              },
              onPersistAllChatReads = { map ->
                RolePreferenceStore.markAllChatThreadsRead(appContext, user.uid, map)
              },
            )
          }
        },
    )

  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }
  val credentialManager = remember { CredentialManager.create(appContext) }
  val scope = rememberCoroutineScope()

  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
  LaunchedEffect(snackbarMessage) {
    val msg = snackbarMessage ?: return@LaunchedEffect
    snackbarHostState.showSnackbar(msg)
    viewModel.consumeSnackbar()
  }

  val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
  val sellerOnlyFilter by viewModel.sellerOnlyFilter.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
  val isAdminResolved by viewModel.isAdminResolved.collectAsStateWithLifecycle()
  val activeCampuses by viewModel.activeCampuses.collectAsStateWithLifecycle()
  val filteredItemsState by viewModel.filteredItemsState.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCampusCode by viewModel.selectedCampusCode.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val mySellingItems by viewModel.mySellingItems.collectAsStateWithLifecycle()
  val buyerTransactions by viewModel.buyerTransactions.collectAsStateWithLifecycle()
  val sellerTransactions by viewModel.sellerTransactions.collectAsStateWithLifecycle()
  val myPremiumRequests by viewModel.myPremiumRequests.collectAsStateWithLifecycle()
  val membershipInfo by viewModel.membershipInfo.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
  val sellDraft by viewModel.sellDraft.collectAsStateWithLifecycle()
  val chatThreads by viewModel.chatThreads.collectAsStateWithLifecycle()
  val chatNotifications by viewModel.chatNotifications.collectAsStateWithLifecycle()
  val unreadChatCount by viewModel.unreadChatCount.collectAsStateWithLifecycle()
  val unreadSellerChatCount by viewModel.unreadSellerChatCount.collectAsStateWithLifecycle()

  val adminCampuses by viewModel.adminCampuses.collectAsStateWithLifecycle()
  val adminPremiumRequests by viewModel.adminPremiumRequests.collectAsStateWithLifecycle()
  val adminUsers by viewModel.adminUsers.collectAsStateWithLifecycle()
  val adminTransactions by viewModel.adminTransactions.collectAsStateWithLifecycle()
  val adminItems by viewModel.adminItems.collectAsStateWithLifecycle()

  val pendingAdminLogin = remember(user.uid) { RolePreferenceStore.consumePendingAdminIntent(appContext) }
  var handledAdminRedirect by remember(user.uid) { mutableStateOf(false) }
  LaunchedEffect(pendingAdminLogin, isAdminResolved, isAdmin) {
    if (pendingAdminLogin && isAdminResolved && !handledAdminRedirect) {
      handledAdminRedirect = true
      if (isAdmin) {
        navController.navigate(Routes.ADMIN) {
          launchSingleTop = true
        }
      } else {
        viewModel.showMessage("Akun Google ini bukan Admin. Masuk ke beranda sebagai pengguna.")
      }
    }
  }

  val mySellingCount = mySellingItems.count { it.status == ItemStatus.AVAILABLE }
  val pendingSellerOrdersCount =
    sellerTransactions.count {
      it.status == TransactionStatus.PENDING_PAYMENT || it.status == TransactionStatus.PAID
    }
  val sellerSoldCount =
    mySellingItems.count { it.status == ItemStatus.SOLD } +
      sellerTransactions.count {
        it.status == TransactionStatus.COMPLETED && it.itemId.startsWith("catalog_")
      }

  Scaffold(
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.navigationBarsPadding(),
      )
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Routes.HOME,
      modifier = Modifier.fillMaxSize().padding(innerPadding),
    ) {
      composable(Routes.HOME) {
        HomeScreen(
          itemsState = filteredItemsState,
          campuses = activeCampuses,
          activeRole = activeRole,
          currentUserId = viewModel.currentUserId,
          sellerOnlyFilter = sellerOnlyFilter,
          mySellingCount = mySellingCount,
          pendingSellerOrdersCount = pendingSellerOrdersCount,
          sellerSoldCount = sellerSoldCount,
          searchQuery = searchQuery,
          selectedCampusCode = selectedCampusCode,
          selectedCategory = selectedCategory,
          isAdmin = isAdmin,
          unreadChatCount = unreadChatCount,
          unreadSellerChatCount = unreadSellerChatCount,
          onSearchChange = viewModel::setSearchQuery,
          onCampusSelect = viewModel::setSelectedCampus,
          onCategorySelect = viewModel::setSelectedCategory,
          onToggleSellerOnly = viewModel::setSellerOnlyFilter,
          onSwitchRole = viewModel::switchRole,
          onItemClick = { item ->
            navController.navigate("detail/${Uri.encode(item.id)}")
          },
          onSellClick = { navController.navigate(Routes.SELL) },
          onManageListingsClick = { navController.navigate(Routes.MY_LISTINGS) },
          onChatClick = { navController.navigate(Routes.CHAT_HISTORY) },
          onHistoryClick = { navController.navigate(Routes.HISTORY) },
          onProfileClick = { navController.navigate(Routes.PROFILE) },
          onAdminClick = { navController.navigate(Routes.ADMIN) },
        )
      }

      composable(
        route = Routes.DETAIL,
        arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
      ) { backStackEntry ->
        val encodedId = backStackEntry.arguments?.getString("itemId").orEmpty()
        val itemId = remember(encodedId) { Uri.decode(encodedId) }
        val item by
          produceState<MarketItem?>(initialValue = null, key1 = itemId) {
            value = viewModel.findItemById(itemId)
          }
        val inquiriesFlow =
          remember(item?.id, item?.sellerId) {
            viewModel.observeInquiries(
              itemId = itemId,
              isSeller = item?.sellerId == viewModel.currentUserId,
            )
          }
        val itemThreads =
          remember(chatThreads, itemId) {
            chatThreads.filter { it.itemId == itemId }
          }
        val itemChatCount = remember(itemThreads) { itemThreads.sumOf { it.messages.size } }

        ItemDetailScreen(
          item = item,
          currentUserId = viewModel.currentUserId,
          activeRole = activeRole,
          isBusy = isBusy,
          inquiriesFlow = inquiriesFlow,
          itemChatCount = itemChatCount,
          onBack = { navController.popBackStack() },
          onBuyClick = { method, meetupNote ->
            val targetItem = item ?: return@ItemDetailScreen
            viewModel.purchaseItem(targetItem, method, meetupNote) { tx ->
              navController.navigate("qr_payment/${Uri.encode(tx.id)}") {
                popUpTo(Routes.HOME) { inclusive = false }
              }
            }
          },
          onSendInquiry = { message ->
            val targetItem = item ?: return@ItemDetailScreen
            viewModel.sendInquiry(targetItem, message)
          },
          onManageMyItem = { navController.navigate(Routes.MY_LISTINGS) },
          onOpenSellerChats = {
            if (itemThreads.size == 1) {
              val t = itemThreads.first()
              navController.navigate(
                "chat_thread/${Uri.encode(t.itemId)}/${Uri.encode(t.buyerId)}/${Uri.encode(t.sellerId)}"
              )
            } else {
              navController.navigate(Routes.CHAT_HISTORY)
            }
          },
          onMarkItemThreadRead = {
            val sellerId = item?.sellerId.orEmpty()
            if (sellerId.isNotBlank()) {
              val key = buildChatThreadKey(itemId, viewModel.currentUserId, sellerId)
              viewModel.markChatThreadAsRead(key)
            }
          },
        )
      }

      composable(Routes.SELL) {
        SellItemScreen(
          campuses = activeCampuses,
          defaultCampusCode = userProfile?.campusCode ?: "UNP",
          draft = sellDraft,
          onUpdateDraft = viewModel::updateSellDraft,
          membershipInfo = membershipInfo,
          isBusy = isBusy,
          onBack = { navController.popBackStack() },
          onOpenPremium = { navController.navigate(Routes.PREMIUM) },
          onPublish = { title, category, campus, price, description, imageUrl, isFeatured ->
            viewModel.publishItem(
              title = title,
              category = category,
              campus = campus,
              price = price,
              description = description,
              imageUrl = imageUrl,
              isFeatured = isFeatured,
              onPublished = { navController.popBackStack() },
            )
          },
        )
      }

      composable(
        route = Routes.QR_PAYMENT,
        arguments = listOf(navArgument("txId") { type = NavType.StringType }),
      ) { backStackEntry ->
        val encodedTxId = backStackEntry.arguments?.getString("txId").orEmpty()
        val txId = remember(encodedTxId) { Uri.decode(encodedTxId) }
        val fetchedTx by
          produceState<MarketTransaction?>(initialValue = null, key1 = txId) {
            value = viewModel.findTransactionById(txId)
          }
        val liveTx =
          remember(txId, buyerTransactions, sellerTransactions, adminTransactions, fetchedTx) {
            buyerTransactions.firstOrNull { it.id == txId }
              ?: sellerTransactions.firstOrNull { it.id == txId }
              ?: adminTransactions.firstOrNull { it.id == txId }
              ?: fetchedTx
          }

        QrPaymentScreen(
          transaction = liveTx,
          currentUserId = viewModel.currentUserId,
          isBusy = isBusy,
          onBack = { navController.popBackStack() },
          onUpdateStatus = { newStatus ->
            viewModel.updateTransactionStatus(txId, newStatus)
          },
          onCopySuccess = {
            viewModel.showMessage("Kode transaksi disalin")
          },
          onDownloadMessage = viewModel::showMessage,
        )
      }

      composable(Routes.HISTORY) {
        HistoryScreen(
          buyerTransactions = buyerTransactions,
          sellerTransactions = sellerTransactions,
          myPremiumRequests = myPremiumRequests,
          adminPremiumRequests = adminPremiumRequests,
          membershipInfo = membershipInfo,
          isAdmin = isAdmin,
          activeRole = activeRole,
          onBack = { navController.popBackStack() },
          onTransactionClick = { tx ->
            navController.navigate("qr_payment/${Uri.encode(tx.id)}")
          },
          onDownloadMessage = viewModel::showMessage,
        )
      }

      composable(Routes.PROFILE) {
        ProfileScreen(
          profile = userProfile,
          membershipInfo = membershipInfo,
          campuses = activeCampuses,
          activeRole = activeRole,
          isAdmin = isAdmin,
          sellingCount = mySellingCount,
          soldCount = sellerSoldCount,
          buyerOrdersCount = buyerTransactions.size,
          unreadChatCount = unreadChatCount,
          onBack = { navController.popBackStack() },
          onSwitchRole = viewModel::switchRole,
          onOpenMyListings = { navController.navigate(Routes.MY_LISTINGS) },
          onOpenHistory = { navController.navigate(Routes.HISTORY) },
          onOpenChatHistory = { navController.navigate(Routes.CHAT_HISTORY) },
          onOpenPremium = { navController.navigate(Routes.PREMIUM) },
          onOpenAdmin = { navController.navigate(Routes.ADMIN) },
          onUpdateProfile = { name, code, campusName, faculty, whatsapp ->
            viewModel.updateProfile(name, code, campusName, faculty, whatsapp)
          },
          onLogout = {
            signOutUser(credentialManager = credentialManager, scope = scope)
          },
        )
      }

      composable(Routes.CHAT_HISTORY) {
        ChatHistoryScreen(
          threads = chatThreads,
          notifications = chatNotifications,
          unreadCount = unreadChatCount,
          currentUserId = viewModel.currentUserId,
          activeRole = activeRole,
          onBack = { navController.popBackStack() },
          onThreadClick = { thread ->
            viewModel.markChatThreadAsRead(thread.threadKey)
            navController.navigate(
              "chat_thread/${Uri.encode(thread.itemId)}/${Uri.encode(thread.buyerId)}/${Uri.encode(thread.sellerId)}"
            )
          },
          onNotificationClick = { notif ->
            viewModel.markChatThreadAsRead(notif.threadKey)
            navController.navigate(
              "chat_thread/${Uri.encode(notif.itemId)}/${Uri.encode(notif.buyerId)}/${Uri.encode(notif.sellerId)}"
            )
          },
          onMarkAllRead = viewModel::markAllChatsAsRead,
        )
      }

      composable(
        route = Routes.CHAT_THREAD,
        arguments = listOf(
          navArgument("itemId") { type = NavType.StringType },
          navArgument("buyerId") { type = NavType.StringType },
          navArgument("sellerId") { type = NavType.StringType },
        ),
      ) { backStackEntry ->
        val itemId = remember(backStackEntry) { Uri.decode(backStackEntry.arguments?.getString("itemId").orEmpty()) }
        val buyerId = remember(backStackEntry) { Uri.decode(backStackEntry.arguments?.getString("buyerId").orEmpty()) }
        val sellerId = remember(backStackEntry) { Uri.decode(backStackEntry.arguments?.getString("sellerId").orEmpty()) }
        val threadKey = remember(itemId, buyerId, sellerId) { buildChatThreadKey(itemId, buyerId, sellerId) }
        val thread = remember(chatThreads, threadKey) { chatThreads.firstOrNull { it.threadKey == threadKey } }
        val fallbackItem by
          produceState<MarketItem?>(initialValue = null, key1 = itemId) {
            value = viewModel.findItemById(itemId)
          }

        ChatDetailScreen(
          itemId = itemId,
          buyerId = buyerId,
          sellerId = sellerId,
          currentUserId = viewModel.currentUserId,
          thread = thread,
          fallbackItem = fallbackItem,
          onBack = { navController.popBackStack() },
          onOpenItem = { id -> navController.navigate("detail/${Uri.encode(id)}") },
          onMarkRead = viewModel::markChatThreadAsRead,
          onSetActiveThread = viewModel::setActiveChatThread,
          onSendMessage = viewModel::sendChatMessage,
        )
      }

      composable(Routes.MY_LISTINGS) {
        MyListingsScreen(
          items = mySellingItems,
          onBack = { navController.popBackStack() },
          onAddNew = { navController.navigate(Routes.SELL) },
          onUpdateDetails = viewModel::updateMyItemDetails,
          onToggleStatus = viewModel::updateMyItemStatus,
          onDelete = viewModel::deleteMyItem,
        )
      }

      composable(Routes.PREMIUM) {
        PremiumScreen(
          profile = userProfile,
          membershipInfo = membershipInfo,
          myRequests = myPremiumRequests,
          isBusy = isBusy,
          onBack = { navController.popBackStack() },
          onSubmitRequest = viewModel::submitPremiumRequest,
          onDownloadMessage = viewModel::showMessage,
        )
      }

      composable(Routes.ADMIN) {
        AdminScreen(
          campuses = adminCampuses.ifEmpty { activeCampuses },
          premiumRequests = adminPremiumRequests,
          users = adminUsers,
          transactions = adminTransactions,
          items = adminItems,
          onBack = { navController.popBackStack() },
          onSaveCampus = { id, code, name, meetupSpot, active ->
            viewModel.saveCampus(id, code, name, meetupSpot, active)
          },
          onDeleteCampus = viewModel::deleteCampus,
          onReviewPremiumRequest = viewModel::reviewPremiumRequest,
          onToggleUserPremium = viewModel::toggleUserPremium,
          onToggleUserVerified = viewModel::toggleUserVerified,
          onUpdateTransactionStatus = viewModel::updateTransactionStatus,
          onModerateItem = viewModel::moderateItemAdmin,
          onDeleteItem = viewModel::deleteItemAdmin,
          onDownloadMessage = viewModel::showMessage,
        )
      }
    }
  }
}

private object Routes {
  const val HOME = "home"
  const val DETAIL = "detail/{itemId}"
  const val SELL = "sell"
  const val QR_PAYMENT = "qr_payment/{txId}"
  const val HISTORY = "history"
  const val CHAT_HISTORY = "chat_history"
  const val CHAT_THREAD = "chat_thread/{itemId}/{buyerId}/{sellerId}"
  const val PROFILE = "profile"
  const val MY_LISTINGS = "my_listings"
  const val PREMIUM = "premium"
  const val ADMIN = "admin"
}
