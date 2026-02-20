package io.umain.munchies.navigation

/**
 * Modal route for filter selection.
 * Displays a list of available filters with multi-select capability.
 */
data class FilterModalRoute(
    val preSelectedFilters: List<String> = emptyList()
) : ModalRoute {
    override val key: String = "filter"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

/**
 * Modal route for submitting a restaurant review.
 * Displays a form to gather review data (rating, comment) for a specific restaurant.
 */
data class SubmitReviewModalRoute(
    val restaurantId: String
) : ModalRoute {
    override val key: String = "submit_review_$restaurantId"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}

/**
 * Modal route for general confirmation dialogs.
 * Displays a message with customizable confirm/cancel buttons.
 */
data class ConfirmActionModalRoute(
    val message: String,
    val confirmText: String = "OK",
    val cancelText: String = "Cancel"
) : ModalRoute {
    override val key: String = "confirm"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.DIALOG
}

/**
 * Modal route for date selection.
 * Displays a date picker for selecting a specific date.
 */
data class DatePickerModalRoute(
    val initialDate: String? = null
) : ModalRoute {
    override val key: String = "date_picker"
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}
