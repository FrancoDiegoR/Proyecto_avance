package com.smartgas_frontend.iam.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.iam.data.repository.ProfileDraft
import com.smartgas_frontend.iam.data.repository.ProfileRepository
import com.smartgas_frontend.iam.domain.model.AccountActivity
import com.smartgas_frontend.iam.domain.model.Profile
import com.smartgas_frontend.iam.domain.model.ProfileStats
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.res
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class ProfileData(
    val profile: Profile? = null,
    val plan: Plan? = null,
    val subscription: Subscription? = null,
    val activity: List<AccountActivity> = emptyList(),
    val stats: ProfileStats = ProfileStats()
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<ProfileData>(isLoading = true))
    val state: State<UIState<ProfileData>> get() = _state

    private val _form = mutableStateOf(ProfileDraft())
    val form: State<ProfileDraft> get() = _form

    private val _editing = mutableStateOf(false)
    val editing: State<Boolean> get() = _editing

    private val _saveLoading = mutableStateOf(false)
    val saveLoading: State<Boolean> get() = _saveLoading

    // Sin sesion la web redirige a /login.
    private val _sessionMissing = mutableStateOf(false)
    val sessionMissing: State<Boolean> get() = _sessionMissing

    init {
        loadData()
    }

    private fun syncFormFromProfile(profile: Profile?) {
        if (profile == null) return

        val session = sessionService.getCurrentUser()

        _form.value = ProfileDraft(
            fullName = profile.fullName,
            email = profile.email.ifEmpty { session?.email.orEmpty() },
            role = profile.role.ifEmpty { session?.role.orEmpty() },
            accountType = profile.accountType.ifEmpty { session?.accountType.orEmpty() },
            businessName = profile.businessName,
            phone = profile.phone,
            district = profile.district,
            address = profile.address
        )
    }

    fun loadData() {
        val accountId = sessionService.getCurrentUser()?.id

        if (accountId == null) {
            _sessionMissing.value = true
            return
        }

        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val profileResult = repository.getProfile(accountId)
            val subscriptionResult = repository.getSubscription(accountId)

            if (profileResult !is Resource.Success || subscriptionResult !is Resource.Success) {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
                return@launch
            }

            val subscription = subscriptionResult.data
            val profile = profileResult.data?.let {
                it.copy(
                    planId = it.planId ?: subscription?.planId,
                    planName = it.planName.ifEmpty { subscription?.planName.orEmpty() }
                )
            }

            syncFormFromProfile(profile)

            val fallbackPlan = Plan(id = 0, name = profile?.planName?.ifEmpty { null } ?: subscription?.planName ?: "Basic")
            val plan = if (profile?.planId != null) {
                repository.getPlan(profile.planId).data ?: fallbackPlan
            } else {
                fallbackPlan
            }

            val activity = async { repository.getActivity(accountId) }
            val stats = async { repository.getStats(accountId) }

            val activityResult = activity.await()
            val statsResult = stats.await()

            if (statsResult is Resource.Success) {
                _state.value = UIState(
                    data = ProfileData(
                        profile = profile,
                        plan = plan,
                        subscription = subscription,
                        activity = activityResult.data.orEmpty(),
                        stats = statsResult.data ?: ProfileStats()
                    )
                )
            } else {
                _state.value = UIState(
                    data = ProfileData(profile = profile, plan = plan, subscription = subscription),
                    message = "An error occurred"
                )
            }
        }
    }

    fun onFormChanged(form: ProfileDraft) {
        _form.value = form
    }

    fun startEdit() {
        _editing.value = true
    }

    fun cancelEdit() {
        _editing.value = false
        syncFormFromProfile(_state.value.data?.profile)
    }

    fun saveProfile() {
        val form = _form.value

        if (form.fullName.isEmpty()) {
            toast(ToastSeverity.Warning, res(R.string.emptyFields))
            return
        }

        _saveLoading.value = true

        viewModelScope.launch {
            val result = repository.updateProfile(sessionService.getAccountId(), form)

            if (result is Resource.Success) {
                _editing.value = false
                loadData()
                toast(ToastSeverity.Success, res(R.string.profileSaved))
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }

            _saveLoading.value = false
        }
    }
}
