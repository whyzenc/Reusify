package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogSeedData
import com.example.data.model.Campus
import com.example.data.model.ChatNotificationItem
import com.example.data.model.ChatThread
import com.example.data.model.ItemInquiry
import com.example.data.model.ItemStatus
import com.example.data.model.MarketItem
import com.example.data.model.MarketTransaction
import com.example.data.model.PaymentMethod
import com.example.data.model.PremiumMembershipInfo
import com.example.data.model.PremiumRequest
import com.example.data.model.SellItemDraft
import com.example.data.model.TransactionStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.buildChatThreadKey
import com.example.data.model.buildChatThreadsAndNotifications
import com.example.data.model.resolvePremiumMembership
import com.example.data.model.sortMarketplaceItems
import com.example.data.repository.ReusifyRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReusifyViewModel(
  private val repository: ReusifyRepository,
  val currentUserId: String,
  private val currentUserName: String,
  private val currentUserEmail: String,
  private val isEmailVerified: Boolean,
  initialRole: UserRole = UserRole.BUYER,
  initialPhone: String = "",
  private val onPersistRole: (UserRole) -> Unit = {},
  initialChatReadMap: Map<String, Long> = emptyMap(),
  private val onPersistChatRead: (String, Long) -> Unit = { _, _ -> },
  private val onPersistAllChatReads: (Map<String, Long>) -> Unit = {},
) : ViewModel() {

  private val _activeRole = MutableStateFlow(initialRole)
  val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

  private val _sellerOnlyFilter = MutableStateFlow(false)
  val sellerOnlyFilter: StateFlow<Boolean> = _sellerOnlyFilter.asStateFlow()

  private val _isAdmin = MutableStateFlow(false)
  val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

  private val _isAdminResolved = MutableStateFlow(false)
  val isAdminResolved: StateFlow<Boolean> = _isAdminResolved.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCampusCode = MutableStateFlow("ALL")
  val selectedCampusCode: StateFlow<String> = _selectedCampusCode.asStateFlow()

  private val _selectedCategory = MutableStateFlow("Semua")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _sellDraft = MutableStateFlow(SellItemDraft())
  val sellDraft: StateFlow<SellItemDraft> = _sellDraft.asStateFlow()

  private val _chatReadTimestamps = MutableStateFlow(initialChatReadMap)
  private val _activeChatThreadKey = MutableStateFlow<String?>(null)

  private val buyerInquiriesFlow: Flow<List<ItemInquiry>> =
    repository.observeBuyerInquiries(currentUserId).catch { error ->
      unwrapAndLog("observeBuyerInquiries", error)
      emit(emptyList())
    }

  private val sellerInquiriesFlow: Flow<List<ItemInquiry>> =
    repository.observeSellerInquiries(currentUserId).catch { error ->
      unwrapAndLog("observeSellerInquiries", error)
      emit(emptyList())
    }

  private val allInquiriesState: StateFlow<List<ItemInquiry>> =
    combine(buyerInquiriesFlow, sellerInquiriesFlow) { buyerMsgs, sellerMsgs ->
      (buyerMsgs + sellerMsgs)
        .distinctBy { it.id.ifBlank { "${it.itemId}_${it.buyerId}_${it.senderId}_${it.message}" } }
        .sortedBy { it.createdAt?.toDate()?.time ?: 0L }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList(),
    )

  init {
    viewModelScope.launch {
      repository.ensureUserProfile(currentUserId, currentUserName, currentUserEmail, initialPhone)
      val adminFlag = repository.checkIsAdmin(currentUserId, currentUserEmail, isEmailVerified)
      _isAdmin.value = adminFlag
      _isAdminResolved.value = true
      if (adminFlag) {
        repository.seedDefaultCampusesIfAdmin(currentUserId)
      }
    }
    viewModelScope.launch {
      val sessionStartMillis = System.currentTimeMillis()
      val seenIds = mutableSetOf<String>()
      allInquiriesState.collect { inquiries ->
        val incoming =
          inquiries.filter {
            val msgMillis = it.createdAt?.toDate()?.time ?: 0L
            it.id.isNotBlank() &&
              it.id !in seenIds &&
              it.senderId != currentUserId &&
              msgMillis >= sessionStartMillis - 10_000L
          }
        inquiries.forEach { if (it.id.isNotBlank()) seenIds.add(it.id) }
        val latest = incoming.lastOrNull()
        if (latest != null) {
          val threadKey = buildChatThreadKey(latest.itemId, latest.buyerId, latest.sellerId)
          if (_activeChatThreadKey.value == threadKey) {
            markChatThreadAsRead(threadKey)
          } else {
            val senderLabel =
              if (latest.senderId == latest.buyerId) latest.buyerName.ifBlank { "Pembeli" }
              else "Penjual"
            _snackbarMessage.value =
              "Pesan baru dari $senderLabel (${latest.itemTitle.take(24)}): \"${latest.message.take(40)}\""
          }
        }
      }
    }
  }

  val userProfile: StateFlow<UserProfile?> =
    repository.observeUserProfile(currentUserId)
      .catch { error ->
        unwrapAndLog("observeUserProfile", error)
        emit(
          UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = currentUserName.ifBlank { "Mahasiswa Padang" },
            email = currentUserEmail,
          )
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue =
          UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = currentUserName.ifBlank { "Mahasiswa Padang" },
            email = currentUserEmail,
          ),
      )

  val activeCampuses: StateFlow<List<Campus>> =
    repository.observeActiveCampuses()
      .catch { error ->
        unwrapAndLog("observeActiveCampuses", error)
        emit(CatalogSeedData.defaultCampuses)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = CatalogSeedData.defaultCampuses,
      )

  val availableItemsState: StateFlow<UiState<List<MarketItem>>> =
    repository.observeAvailableItems()
      .map<List<MarketItem>, UiState<List<MarketItem>>> { UiState.Success(it) }
      .catch { error ->
        val root = unwrapAndLog("observeAvailableItems", error)
        emit(UiState.Error(root.message ?: "Gagal memuat daftar barang"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = UiState.Loading,
      )

  val mySellingItems: StateFlow<List<MarketItem>> =
    repository.observeMySellingItems(currentUserId)
      .catch { error ->
        unwrapAndLog("observeMySellingItems", error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = emptyList(),
      )

  val buyerTransactions: StateFlow<List<MarketTransaction>> =
    repository.observeBuyerTransactions(currentUserId)
      .catch { error ->
        unwrapAndLog("observeBuyerTransactions", error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = emptyList(),
      )

  val sellerTransactions: StateFlow<List<MarketTransaction>> =
    repository.observeSellerTransactions(currentUserId)
      .catch { error ->
        unwrapAndLog("observeSellerTransactions", error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = emptyList(),
      )

  val myPremiumRequests: StateFlow<List<PremiumRequest>> =
    repository.observeMyPremiumRequests(currentUserId)
      .catch { error ->
        unwrapAndLog("observeMyPremiumRequests", error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = emptyList(),
      )

  val membershipInfo: StateFlow<PremiumMembershipInfo> =
    combine(userProfile, myPremiumRequests) { profile, requests ->
      resolvePremiumMembership(profile, requests)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
      initialValue = PremiumMembershipInfo(),
    )

  // Filtered marketplace items combining search, campus, category, seller-store filter, and excluding already-bought catalog items
  val filteredItemsState: StateFlow<UiState<List<MarketItem>>> =
    combine(
      combine(availableItemsState, _searchQuery, _selectedCampusCode, _selectedCategory) {
        itemsState, query, campusCode, category ->
        Quadruple(itemsState, query, campusCode, category)
      },
      buyerTransactions,
      _activeRole,
      _sellerOnlyFilter,
    ) { (itemsState, query, campusCode, category), purchases, role, sellerOnly ->
      when (itemsState) {
        is UiState.Loading -> UiState.Loading
        is UiState.Error -> itemsState
        is UiState.Success -> {
          val purchasedCatalogIds =
            purchases
              .filter { it.itemId.startsWith("catalog_") && it.status != TransactionStatus.CANCELLED }
              .map { it.itemId }
              .toSet()

          val cleanQuery = query.trim().lowercase()
          val filtered =
            itemsState.data.filter { item ->
              val notBoughtCatalog = item.id !in purchasedCatalogIds
              val matchesSellerScope =
                !(role == UserRole.SELLER && sellerOnly) || item.sellerId == currentUserId
              val matchesCampus =
                campusCode == "ALL" || item.campusCode.equals(campusCode, ignoreCase = true)
              val matchesCategory =
                category == "Semua" || item.category.equals(category, ignoreCase = true)
              val matchesQuery =
                cleanQuery.isEmpty() ||
                  item.title.lowercase().contains(cleanQuery) ||
                  item.description.lowercase().contains(cleanQuery) ||
                  item.category.lowercase().contains(cleanQuery) ||
                  item.campusCode.lowercase().contains(cleanQuery)
              notBoughtCatalog && matchesSellerScope && matchesCampus && matchesCategory && matchesQuery
            }
          UiState.Success(sortMarketplaceItems(filtered))
        }
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
      initialValue = UiState.Loading,
    )

  private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

  // --- ADMIN FLOWS (only subscribed when isAdmin == true) ---

  val adminCampuses: StateFlow<List<Campus>> =
    _isAdmin
      .flatMapLatest { admin ->
        if (admin) repository.observeAllCampusesAdmin() else flowOf(emptyList())
      }
      .catch { error ->
        unwrapAndLog("adminCampuses", error)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val adminPremiumRequests: StateFlow<List<PremiumRequest>> =
    _isAdmin
      .flatMapLatest { admin ->
        if (admin) repository.observeAllPremiumRequestsAdmin() else flowOf(emptyList())
      }
      .catch { error ->
        unwrapAndLog("adminPremiumRequests", error)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val adminUsers: StateFlow<List<UserProfile>> =
    _isAdmin
      .flatMapLatest { admin ->
        if (admin) repository.observeAllUsersAdmin() else flowOf(emptyList())
      }
      .catch { error ->
        unwrapAndLog("adminUsers", error)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val adminTransactions: StateFlow<List<MarketTransaction>> =
    _isAdmin
      .flatMapLatest { admin ->
        if (admin) repository.observeAllTransactionsAdmin() else flowOf(emptyList())
      }
      .catch { error ->
        unwrapAndLog("adminTransactions", error)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val adminItems: StateFlow<List<MarketItem>> =
    _isAdmin
      .flatMapLatest { admin ->
        if (admin) repository.observeAllItemsAdmin() else flowOf(emptyList())
      }
      .catch { error ->
        unwrapAndLog("adminItems", error)
        emit(emptyList())
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  // --- FILTER & ROLE ACTIONS ---

  fun switchRole(role: UserRole) {
    if (_activeRole.value == role) return
    _activeRole.value = role
    if (role == UserRole.BUYER) {
      _sellerOnlyFilter.value = false
    }
    onPersistRole(role)
    _snackbarMessage.value = "Beralih ke Mode ${role.label}"
  }

  fun setSellerOnlyFilter(onlyMine: Boolean) {
    _sellerOnlyFilter.value = onlyMine
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedCampus(code: String) {
    _selectedCampusCode.value = code
  }

  fun setSelectedCategory(category: String) {
    _selectedCategory.value = category
  }

  fun consumeSnackbar() {
    _snackbarMessage.value = null
  }

  fun showMessage(msg: String) {
    _snackbarMessage.value = msg
  }

  fun updateSellDraft(transform: (SellItemDraft) -> SellItemDraft) {
    _sellDraft.value = transform(_sellDraft.value)
  }

  fun clearSellDraft() {
    _sellDraft.value = SellItemDraft()
  }

  // --- PROFILE ACTIONS ---

  fun updateProfile(
    displayName: String,
    campusCode: String,
    campusName: String,
    faculty: String,
    whatsapp: String,
    onSuccess: () -> Unit = {},
  ) {
    if (displayName.isBlank()) {
      _snackbarMessage.value = "Nama lengkap wajib diisi"
      return
    }
    viewModelScope.launch {
      _isBusy.value = true
      val res =
        repository.updateUserProfile(
          uid = currentUserId,
          displayName = displayName,
          campusCode = campusCode,
          campusName = campusName,
          faculty = faculty,
          whatsapp = whatsapp,
        )
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value = "Profil berhasil diperbarui"
          onSuccess()
        },
        onFailure = {
          _snackbarMessage.value = "Gagal memperbarui profil: ${it.localizedMessage}"
        },
      )
    }
  }

  // --- LISTING / SELLING ACTIONS ---

  fun publishItem(
    title: String,
    category: String,
    campus: Campus,
    price: Long,
    description: String,
    imageUrl: String,
    isFeatured: Boolean,
    onPublished: () -> Unit,
  ) {
    val profile = userProfile.value ?: return
    viewModelScope.launch {
      _isBusy.value = true
      val res =
        repository.createItem(
          title = title,
          category = category,
          campusCode = campus.code,
          campusName = campus.name,
          price = price,
          description = description,
          imageUrl = imageUrl,
          sellerName = profile.displayName.ifBlank { currentUserName },
          sellerCampusCode = profile.campusCode,
          sellerPremium = membershipInfo.value.isActive,
          isFeatured = membershipInfo.value.isActive && isFeatured,
        )
      _isBusy.value = false
      res.fold(
        onSuccess = {
          clearSellDraft()
          _snackbarMessage.value = "Iklan barang berhasil dipasang!"
          onPublished()
        },
        onFailure = {
          _snackbarMessage.value = "Gagal memasang iklan: ${it.localizedMessage}"
        },
      )
    }
  }

  fun updateMyItemStatus(item: MarketItem, newStatus: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.updateMyItem(item.copy(status = newStatus))
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value =
            when (newStatus) {
              ItemStatus.AVAILABLE -> "Barang diaktifkan kembali"
              ItemStatus.ARCHIVED -> "Barang diarsipkan"
              else -> "Status barang diperbarui"
            }
        },
        onFailure = { _snackbarMessage.value = "Gagal mengubah status: ${it.localizedMessage}" },
      )
    }
  }

  fun updateMyItemDetails(item: MarketItem, newTitle: String, newPrice: Long, newDesc: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res =
        repository.updateMyItem(
          item.copy(
            title = newTitle.trim(),
            price = newPrice,
            description = newDesc.trim(),
          )
        )
      _isBusy.value = false
      res.fold(
        onSuccess = { _snackbarMessage.value = "Detail barang diperbarui" },
        onFailure = { _snackbarMessage.value = "Gagal memperbarui barang: ${it.localizedMessage}" },
      )
    }
  }

  fun deleteMyItem(itemId: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.deleteItem(itemId)
      _isBusy.value = false
      res.fold(
        onSuccess = { _snackbarMessage.value = "Barang dihapus dari daftar jualan" },
        onFailure = { _snackbarMessage.value = "Gagal menghapus barang: ${it.localizedMessage}" },
      )
    }
  }

  // --- PURCHASE & QR PAYMENT ACTIONS ---

  suspend fun findItemById(itemId: String): MarketItem? {
    val cached =
      (availableItemsState.value as? UiState.Success)?.data?.firstOrNull { it.id == itemId }
        ?: mySellingItems.value.firstOrNull { it.id == itemId }
    if (cached != null) return cached
    return repository.getItemById(itemId).getOrNull()
  }

  suspend fun findTransactionById(txId: String): MarketTransaction? {
    val cached =
      buyerTransactions.value.firstOrNull { it.id == txId }
        ?: sellerTransactions.value.firstOrNull { it.id == txId }
        ?: adminTransactions.value.firstOrNull { it.id == txId }
    if (cached != null) return cached
    return repository.getTransactionById(txId).getOrNull()
  }

  fun purchaseItem(
    item: MarketItem,
    paymentMethod: PaymentMethod,
    meetupNote: String,
    onTransactionCreated: (MarketTransaction) -> Unit,
  ) {
    val profile =
      userProfile.value
        ?: UserProfile(
          userId = currentUserId,
          displayName = currentUserName.ifBlank { "Mahasiswa" },
          email = currentUserEmail,
        )
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.purchaseItem(item, profile, paymentMethod, meetupNote)
      _isBusy.value = false
      res.fold(
        onSuccess = { tx ->
          _snackbarMessage.value = "Pesanan dibuat! Kode: ${tx.transactionCode}"
          onTransactionCreated(tx)
        },
        onFailure = {
          _snackbarMessage.value = it.localizedMessage ?: "Gagal membuat pesanan"
        },
      )
    }
  }

  fun updateTransactionStatus(txId: String, newStatus: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.updateTransactionStatus(txId, newStatus)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value =
            when (newStatus) {
              TransactionStatus.PAID -> "Pembayaran berhasil dikonfirmasi!"
              TransactionStatus.COMPLETED -> "Transaksi selesai & tercatat di riwayat!"
              TransactionStatus.CANCELLED -> "Transaksi dibatalkan"
              else -> "Status transaksi diperbarui"
            }
        },
        onFailure = {
          _snackbarMessage.value = "Gagal memperbarui transaksi: ${it.localizedMessage}"
        },
      )
    }
  }

  // --- PREMIUM REQUESTS ---

  fun submitPremiumRequest(planName: String, paymentCode: String) {
    val profile = userProfile.value ?: return
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.submitPremiumRequest(profile, planName, paymentCode)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value = "Pengajuan Premium terkirim! Menunggu verifikasi Admin."
        },
        onFailure = {
          _snackbarMessage.value = "Gagal mengirim pengajuan: ${it.localizedMessage}"
        },
      )
    }
  }

  // --- ADMIN ACTIONS ---

  fun saveCampus(
    campusId: String?,
    code: String,
    name: String,
    meetupSpot: String,
    active: Boolean,
    onSaved: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.addOrUpdateCampus(campusId, code, name, meetupSpot, active)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value = "Kampus $code berhasil disimpan"
          onSaved()
        },
        onFailure = {
          _snackbarMessage.value = "Gagal menyimpan kampus: ${it.localizedMessage}"
        },
      )
    }
  }

  fun deleteCampus(campusId: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.deleteCampus(campusId)
      _isBusy.value = false
      res.fold(
        onSuccess = { _snackbarMessage.value = "Kampus dihapus" },
        onFailure = { _snackbarMessage.value = "Gagal menghapus kampus: ${it.localizedMessage}" },
      )
    }
  }

  fun reviewPremiumRequest(request: PremiumRequest, approve: Boolean) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.reviewPremiumRequest(request, approve)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value =
            if (approve) "Akun Premium ${request.userName} diaktifkan!"
            else "Pengajuan Premium ditolak."
        },
        onFailure = { _snackbarMessage.value = "Gagal memproses: ${it.localizedMessage}" },
      )
    }
  }

  fun toggleUserPremium(user: UserProfile, enable: Boolean) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.setUserPremiumAdmin(user.userId, enable)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value =
            if (enable) "${user.displayName} kini berstatus Premium"
            else "Status Premium ${user.displayName} dinonaktifkan"
        },
        onFailure = { _snackbarMessage.value = "Gagal mengubah status: ${it.localizedMessage}" },
      )
    }
  }

  fun toggleUserVerified(user: UserProfile, verified: Boolean) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.setUserVerifiedAdmin(user.userId, verified)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value =
            if (verified) "Akun mahasiswa ${user.displayName} terverifikasi"
            else "Verifikasi mahasiswa ${user.displayName} dicabut"
        },
        onFailure = { _snackbarMessage.value = "Gagal mengubah verifikasi: ${it.localizedMessage}" },
      )
    }
  }

  fun moderateItemAdmin(item: MarketItem, isFeatured: Boolean, status: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.updateItemModerationAdmin(item.id, isFeatured, status)
      _isBusy.value = false
      res.fold(
        onSuccess = {
          _snackbarMessage.value = "Moderasi iklan '${item.title}' diperbarui"
        },
        onFailure = { _snackbarMessage.value = "Gagal moderasi iklan: ${it.localizedMessage}" },
      )
    }
  }

  fun deleteItemAdmin(itemId: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.deleteItem(itemId)
      _isBusy.value = false
      res.fold(
        onSuccess = { _snackbarMessage.value = "Iklan dihapus oleh Admin" },
        onFailure = { _snackbarMessage.value = "Gagal menghapus iklan: ${it.localizedMessage}" },
      )
    }
  }

  // --- INQUIRIES / CHAT ---

  private val chatBundleState: StateFlow<Pair<List<ChatThread>, List<ChatNotificationItem>>> =
    combine(
      allInquiriesState,
      _chatReadTimestamps,
      availableItemsState,
      mySellingItems,
      buyerTransactions,
    ) { inquiries, readMap, availState, myItems, buyerTxs ->
      val availableList = (availState as? UiState.Success)?.data ?: CatalogSeedData.catalogItems
      val itemsById = (availableList + myItems).associateBy { it.id }
      val sellerNamesByTxItem =
        buyerTxs.associate { it.itemId to it.sellerName }.filterValues { it.isNotBlank() }
      buildChatThreadsAndNotifications(
        inquiries = inquiries,
        currentUserId = currentUserId,
        readTimestamps = readMap,
        itemsById = itemsById,
        sellerNamesByTxItem = sellerNamesByTxItem,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList<ChatThread>() to emptyList(),
    )

  val chatThreads: StateFlow<List<ChatThread>> =
    chatBundleState
      .map { it.first }
      .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val chatNotifications: StateFlow<List<ChatNotificationItem>> =
    chatBundleState
      .map { it.second }
      .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val unreadChatCount: StateFlow<Int> =
    chatThreads
      .map { list -> list.sumOf { it.unreadCount } }
      .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  val unreadSellerChatCount: StateFlow<Int> =
    chatThreads
      .map { list -> list.filter { it.isViewerSeller }.sumOf { it.unreadCount } }
      .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  fun setActiveChatThread(threadKey: String?) {
    _activeChatThreadKey.value = threadKey
    if (!threadKey.isNullOrBlank()) {
      markChatThreadAsRead(threadKey)
    }
  }

  fun markChatThreadAsRead(threadKey: String) {
    if (threadKey.isBlank()) return
    val threadMaxMillis =
      chatThreads.value
        .firstOrNull { it.threadKey == threadKey }
        ?.messages
        ?.maxOfOrNull { it.createdAt?.toDate()?.time ?: 0L }
        ?: 0L
    val readAt = maxOf(System.currentTimeMillis(), threadMaxMillis)
    val updated = _chatReadTimestamps.value.toMutableMap()
    if ((updated[threadKey] ?: 0L) < readAt) {
      updated[threadKey] = readAt
      _chatReadTimestamps.value = updated
      onPersistChatRead(threadKey, readAt)
    }
  }

  fun markAllChatsAsRead() {
    val now = System.currentTimeMillis()
    val updated = _chatReadTimestamps.value.toMutableMap()
    val newEntries = mutableMapOf<String, Long>()
    chatThreads.value.forEach { thread ->
      val threadMaxMillis = thread.messages.maxOfOrNull { it.createdAt?.toDate()?.time ?: 0L } ?: 0L
      val readAt = maxOf(now, threadMaxMillis)
      if ((updated[thread.threadKey] ?: 0L) < readAt) {
        updated[thread.threadKey] = readAt
        newEntries[thread.threadKey] = readAt
      }
    }
    if (newEntries.isNotEmpty()) {
      _chatReadTimestamps.value = updated
      onPersistAllChatReads(newEntries)
      _snackbarMessage.value = "Semua notifikasi chat ditandai terbaca"
    }
  }

  fun observeInquiries(itemId: String, isSeller: Boolean): Flow<List<ItemInquiry>> =
    repository.observeInquiriesForItem(itemId, currentUserId, isSeller).catch { error ->
      unwrapAndLog("observeInquiries", error)
      emit(emptyList())
    }

  fun sendInquiry(item: MarketItem, message: String) {
    if (message.isBlank()) return
    val name = userProfile.value?.displayName ?: currentUserName
    viewModelScope.launch {
      val res = repository.sendInquiry(item, currentUserId, name, message)
      res.onFailure {
        _snackbarMessage.value = "Gagal mengirim pesan: ${it.localizedMessage}"
      }
    }
  }

  fun sendChatMessage(
    itemId: String,
    itemTitle: String,
    buyerId: String,
    buyerName: String,
    sellerId: String,
    message: String,
  ) {
    if (message.isBlank()) return
    val cleanBuyerName =
      if (currentUserId == buyerId) (userProfile.value?.displayName ?: currentUserName)
      else buyerName
    viewModelScope.launch {
      val res =
        repository.sendChatMessage(
          itemId = itemId,
          itemTitle = itemTitle,
          buyerId = buyerId,
          buyerName = cleanBuyerName,
          sellerId = sellerId,
          message = message,
        )
      res.onFailure {
        _snackbarMessage.value = "Gagal mengirim pesan: ${it.localizedMessage}"
      }
    }
  }

  private fun unwrapAndLog(source: String, error: Throwable): Throwable {
    val firestoreError =
      generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val root = firestoreError ?: error
    Log.w(TAG, "Error in $source", root)
    return root
  }

  private companion object {
    const val TAG = "ReusifyVM"
    const val STOP_TIMEOUT_MS = 5000L
  }
}
