package com.smartgas_frontend.paymentmanagement.presentation.plans

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smartgas_frontend.R
import com.smartgas_frontend.common.Resource
import com.smartgas_frontend.common.UIState
import com.smartgas_frontend.paymentmanagement.data.repository.SubscriptionRepository
import com.smartgas_frontend.paymentmanagement.domain.model.Plan
import com.smartgas_frontend.paymentmanagement.domain.model.Subscription
import com.smartgas_frontend.paymentmanagement.domain.model.SubscriptionRequest
import com.smartgas_frontend.shared.data.local.SessionService
import com.smartgas_frontend.shared.presentation.components.ToastSeverity
import com.smartgas_frontend.shared.presentation.components.ToastViewModel
import com.smartgas_frontend.shared.presentation.components.res
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class SubscriptionData(
    val plans: List<Plan> = emptyList(),
    val subscription: Subscription? = null
) {
    val currentPlan: Plan? get() = plans.find { subscription != null && it.id == subscription.planId }
}

class PlansViewModel(
    private val repository: SubscriptionRepository,
    private val sessionService: SessionService
) : ToastViewModel() {

    private val _state = mutableStateOf(UIState<SubscriptionData>(isLoading = true))
    val state: State<UIState<SubscriptionData>> get() = _state

    private val _selectedPlan = mutableStateOf<Plan?>(null)
    val selectedPlan: State<Plan?> get() = _selectedPlan

    private val _showConfirmDialog = mutableStateOf(false)
    val showConfirmDialog: State<Boolean> get() = _showConfirmDialog

    private val _confirmLoading = mutableStateOf(false)
    val confirmLoading: State<Boolean> get() = _confirmLoading

    init {
        loadData()
    }

    fun loadData() {
        _state.value = UIState(isLoading = true, data = _state.value.data)

        viewModelScope.launch {
            val plans = async { repository.getPlans() }
            val subscription = async { repository.getSubscription(sessionService.getAccountId()) }

            val plansResult = plans.await()
            val subscriptionResult = subscription.await()

            if (plansResult is Resource.Success && subscriptionResult is Resource.Success) {
                _state.value = UIState(
                    data = SubscriptionData(
                        plans = plansResult.data.orEmpty(),
                        subscription = subscriptionResult.data
                    )
                )
            } else {
                _state.value = UIState(data = _state.value.data, message = "An error occurred")
            }
        }
    }

    fun openRequest(plan: Plan) {
        _selectedPlan.value = plan
        _showConfirmDialog.value = true
    }

    fun closeRequest() {
        _showConfirmDialog.value = false
    }

    fun confirmRequest() {
        val plan = _selectedPlan.value ?: return

        _confirmLoading.value = true

        viewModelScope.launch {
            val result = repository.createRequest(
                SubscriptionRequest(accountId = sessionService.getAccountId(), planId = plan.id)
            )

            if (result is Resource.Success) {
                _showConfirmDialog.value = false
                _selectedPlan.value = null
                loadData()
                toast(ToastSeverity.Success, res(R.string.planChangeApplied))
            } else {
                toast(ToastSeverity.Error, res(R.string.errorSaving))
            }

            _confirmLoading.value = false
        }
    }
}
