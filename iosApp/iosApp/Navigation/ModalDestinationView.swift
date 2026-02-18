import SwiftUI
import shared

/// Maps KMP ModalDestination to iOS modal UI.
/// Handles presentation styles (sheet, fullscreen, dialog) and dismissal logic.
struct ModalDestinationView: View {
    let modal: shared.ModalRoute
    let onDismiss: () -> Void
    
    var body: some View {
        Group {
            switch modal.presentationStyle {
            case .sheet:
                sheetContent()
            case .fullScreen:
                fullScreenContent()
            case .dialog:
                dialogContent()
            @unknown default:
                Text("Unknown modal type")
            }
        }
    }
    
    @ViewBuilder
    private func sheetContent() -> some View {
        if let filter = modal as? ModalDestination_Filter {
            FilterModalView(modal: filter, onDismiss: onDismiss)
        } else if let confirm = modal as? ModalDestination_ConfirmAction {
            ConfirmActionModalView(modal: confirm, onDismiss: onDismiss)
        } else if let reviews = modal as? ModalDestination_Reviews {
            ReviewsModalView(modal: reviews, onDismiss: onDismiss)
        } else if let datePicker = modal as? ModalDestination_DatePicker {
            DatePickerModalView(modal: datePicker, onDismiss: onDismiss)
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    private func fullScreenContent() -> some View {
        if let reviews = modal as? ModalDestination_Reviews {
            ReviewsModalView(modal: reviews, onDismiss: onDismiss)
                .ignoresSafeArea()
        } else {
            sheetContent()
        }
    }
    
    @ViewBuilder
    private func dialogContent() -> some View {
        if let confirm = modal as? ModalDestination_ConfirmAction {
            ConfirmActionModalView(modal: confirm, onDismiss: onDismiss)
        } else {
            sheetContent()
        }
    }
}

// MARK: - Filter Modal

struct FilterModalView: View {
    let modal: ModalDestination_Filter
    let onDismiss: () -> Void
    
    @State private var selectedFilters = Set<String>()
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack {
                    Text("Filters")
                        .font(.headline)
                    Spacer()
                    Button(action: onDismiss) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                
                Divider()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("No filters available")
                            .foregroundColor(.gray)
                            .padding()
                    }
                }
                
                Divider()
                
                HStack(spacing: 12) {
                    Button(action: onDismiss) {
                        Text("Cancel")
                            .frame(maxWidth: .infinity)
                            .padding(12)
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                    }
                    
                    Button(action: onDismiss) {
                        Text("Apply")
                            .frame(maxWidth: .infinity)
                            .padding(12)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                }
                .padding(16)
            }
        }
        .onAppear {
            selectedFilters = Set(modal.preSelectedFilters)
        }
    }
}

// MARK: - Confirm Action Modal

struct ConfirmActionModalView: View {
    let modal: ModalDestination_ConfirmAction
    let onDismiss: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Text(modal.message)
                .font(.body)
                .multilineTextAlignment(.center)
            
            HStack(spacing: 12) {
                Button(action: onDismiss) {
                    Text(modal.cancelText)
                        .frame(maxWidth: .infinity)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                }
                
                Button(action: onDismiss) {
                    Text(modal.confirmText)
                        .frame(maxWidth: .infinity)
                        .padding(12)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
        }
        .padding(20)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .padding(16)
    }
}

// MARK: - Reviews Modal

struct ReviewsModalView: View {
    let modal: ModalDestination_Reviews
    let onDismiss: () -> Void
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack {
                    Text("Reviews")
                        .font(.headline)
                    Spacer()
                    Button(action: onDismiss) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                
                Divider()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("No reviews for restaurant \(modal.restaurantId)")
                            .foregroundColor(.gray)
                            .padding()
                    }
                }
            }
        }
    }
}

// MARK: - Date Picker Modal

struct DatePickerModalView: View {
    let modal: ModalDestination_DatePicker
    let onDismiss: () -> Void
    
    @State private var selectedDate = Date()
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                DatePicker(
                    "Select Date",
                    selection: $selectedDate,
                    displayedComponents: [.date]
                )
                .datePickerStyle(.graphical)
                
                HStack(spacing: 12) {
                    Button(action: onDismiss) {
                        Text("Cancel")
                            .frame(maxWidth: .infinity)
                            .padding(12)
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                    }
                    
                    Button(action: onDismiss) {
                        Text("Done")
                            .frame(maxWidth: .infinity)
                            .padding(12)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                }
            }
            .padding(16)
        }
    }
}
