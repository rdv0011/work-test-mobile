import SwiftUI
import shared

/// Maps KMP ModalDestination to iOS modal UI.
/// Handles presentation styles (sheet, fullscreen, dialog) and dismissal logic.
struct ModalDestinationView: View {
    let modal: shared.ModalRoute
    let onDismiss: () -> Void
    let viewModel: RestaurantDetailViewModel?
    
    init(modal: shared.ModalRoute, onDismiss: @escaping () -> Void, viewModel: RestaurantDetailViewModel? = nil) {
        self.modal = modal
        self.onDismiss = onDismiss
        self.viewModel = viewModel
    }
    
    var body: some View {
        Group {
            switch modal.presentationStyle {
            case .sheet:
                sheetContent()
            case .fullScreen:
                fullScreenContent()
            case .dialog:
                dialogContent()
            default:
                Text("Unknown modal type")
            }
        }
    }
    
    @ViewBuilder
    private func sheetContent() -> some View {
        if let filter = modal as? FilterModalRoute {
            FilterModalView(modal: filter, onDismiss: onDismiss)
        } else if let reviews = modal as? SubmitReviewModalRoute {
            ReviewsModalView(modal: reviews, onDismiss: onDismiss, viewModel: viewModel)
        } else if let datePicker = modal as? DatePickerModalRoute {
            DatePickerModalView(modal: datePicker, onDismiss: onDismiss)
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    private func fullScreenContent() -> some View {
        if let reviews = modal as? SubmitReviewModalRoute {
            ReviewsModalView(modal: reviews, onDismiss: onDismiss, viewModel: viewModel)
                .ignoresSafeArea()
        } else {
            sheetContent()
        }
    }
    
    @ViewBuilder
    private func dialogContent() -> some View {
        if let confirm = modal as? ConfirmActionModalRoute {
            ConfirmActionModalView(modal: confirm, onDismiss: onDismiss)
        } else {
            sheetContent()
        }
    }
}

// MARK: - Filter Modal

struct FilterModalView: View {
    let modal: FilterModalRoute
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
    let modal: ConfirmActionModalRoute
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
    let modal: SubmitReviewModalRoute
    let onDismiss: () -> Void
    
    @State private var rating: Int = 5
    @State private var comment: String = ""
    @State private var isSubmitting: Bool = false
    
    let viewModel: RestaurantDetailViewModel?
    
    init(modal: SubmitReviewModalRoute, onDismiss: @escaping () -> Void, viewModel: RestaurantDetailViewModel? = nil) {
        self.modal = modal
        self.onDismiss = onDismiss
        self.viewModel = viewModel
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack {
                    Text("Leave a Review")
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
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Rate your experience")
                            .font(.body)
                            .foregroundColor(.gray)
                            .padding(.horizontal, 16)
                            .padding(.top, 16)
                        
                        HStack(spacing: 4) {
                            ForEach(1...5, id: \.self) { star in
                                Image(systemName: star <= rating ? "star.fill" : "star")
                                    .foregroundColor(star <= rating ? Color(0xFFB800) : Color.gray)
                                    .font(.system(size: 24))
                            }
                        }
                        .padding(.horizontal, 16)
                        
                        HStack(spacing: 8) {
                            ForEach(1...5, id: \.self) { num in
                                Button(action: { rating = num }) {
                                    Text("\(num)")
                                        .frame(maxWidth: .infinity)
                                        .padding(8)
                                        .background(num <= rating ? Color.blue : Color(.systemGray6))
                                        .foregroundColor(num <= rating ? .white : .primary)
                                        .cornerRadius(6)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        
                        Text("Add your comment")
                            .font(.body)
                            .foregroundColor(.gray)
                            .padding(.horizontal, 16)
                        
                        TextEditor(text: $comment)
                            .frame(height: 120)
                            .border(Color(.systemGray3), width: 1)
                            .cornerRadius(8)
                            .padding(.horizontal, 16)
                            .scrollContentBackground(.hidden)
                        
                        Text("Share your experience...")
                            .font(.caption)
                            .foregroundColor(.gray)
                            .padding(.horizontal, 16)
                            .padding(.bottom, 16)
                    }
                }
                
                Divider()
                
                HStack(spacing: 12) {
                    Button(action: onDismiss) {
                        Text("Cancel")
                            .frame(maxWidth: .infinity)
                            .padding(12)
                            .background(Color(.systemGray6))
                            .foregroundColor(.primary)
                            .cornerRadius(8)
                    }
                    .disabled(isSubmitting)
                    
                    Button(action: submitReview) {
                        Text("Submit Review")
                            .frame(maxWidth: .infinity)
                            .padding(12)
                            .background(comment.trimmingCharacters(in: .whitespaces).isEmpty ? Color(.systemGray6) : Color.blue)
                            .foregroundColor(comment.trimmingCharacters(in: .whitespaces).isEmpty ? Color(.systemGray) : .white)
                            .cornerRadius(8)
                    }
                    .disabled(isSubmitting || comment.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                .padding(16)
            }
        }
    }
    
    private func submitReview() {
        guard let viewModel = viewModel, !comment.trimmingCharacters(in: .whitespaces).isEmpty else {
            return
        }
        
        isSubmitting = true
        viewModel.submitReview(rating: Int32(rating), comment: comment)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            onDismiss()
        }
    }
}

// MARK: - Date Picker Modal

struct DatePickerModalView: View {
    let modal: DatePickerModalRoute
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
