package io.umain.munchies.navigation

import kotlinx.serialization.Serializable

@Serializable
data class FilterModalRoute(
    val preSelectedFilters: List<String> = emptyList()
) : ModalRoute() {
    override val key: String = "filter"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

@Serializable
data class SubmitReviewModalRoute(
    val restaurantId: String
) : ModalRoute() {
    override val key: String = "submit_review_$restaurantId"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

@Serializable
data class ConfirmActionModalRoute(
    val message: String,
    val confirmText: String = "OK",
    val cancelText: String = "Cancel"
) : ModalRoute() {
    override val key: String = "confirm"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.DIALOG
}

@Serializable
data class DatePickerModalRoute(
    val initialDate: String? = null
) : ModalRoute() {
    override val key: String = "date_picker"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

@Serializable
data object ReviewSuccessModalRoute : ModalRoute() {
    override val key: String = "review_success"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.DIALOG
}

@Serializable
data class ReviewErrorAlertRoute(
    val message: String
) : ModalRoute() {
    override val key: String = "review_error"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.DIALOG
}
