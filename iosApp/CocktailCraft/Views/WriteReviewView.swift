import SwiftUI

/// Write-a-review bottom sheet, laid out like Android's WriteReviewSheet (and
/// like the Advanced Search sheets): a Cancel / title header, name, a 1-5
/// star picker, a comment capped at 500 characters, then Submit Review. Name and rating
/// are checked here (same as Android); comment rules are enforced by the
/// shared ReviewViewModel and its rejection message is shown inline via
/// `submissionError`, keeping the sheet open so nothing typed is lost.
struct WriteReviewView: View {
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance
    @State private var userName = ""
    @State private var rating = 0
    @State private var comment = ""
    @State private var showFieldErrors = false
    @State private var isSubmitting = false
    /// Measured form height: the sheet is only as tall as its content, like
    /// Android's bottom sheet (the system default would fill the screen)
    @State private var contentHeight: CGFloat = 600

    /// Latest rejection from the shared ViewModel (validation or save failure).
    let submissionError: String?
    let onDismiss: () -> Void
    /// Async so the button can hold its spinner across the real submit call
    /// (same contract as EditProfileView).
    let onSubmit: (_ userName: String, _ rating: Int, _ comment: String) async -> Void
    /// Clears the shared error once the user edits the form again.
    let onClearError: () -> Void

    static let maxCommentLength = 500

    private var nameMissing: Bool {
        userName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    private var ratingMissing: Bool { rating == 0 }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: AppTheme.Spacing.xl) {
                header
                nameField
                ratingPicker
                commentField

                if let submissionError {
                    errorText(submissionError)
                        .accessibilityIdentifier("writeReview.errorMessage")
                }

                PrimaryButton(
                    title: L(\.review_submit),
                    isLoading: isSubmitting,
                    action: submit
                )
                .accessibilityIdentifier("writeReview.submitButton")
            }
            .padding(.horizontal)
            // Clear the drag indicator, as Android's sheet header does
            .padding(.top, AppTheme.Spacing.xl)
            .padding(.bottom, AppTheme.Spacing.xl)
            .background(
                GeometryReader { proxy in
                    Color.clear
                        .onAppear { contentHeight = proxy.size.height }
                        .onChange(of: proxy.size.height) { _, newHeight in contentHeight = newHeight }
                }
            )
        }
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .presentationBackground(AppColors.surface(isDarkMode: isDarkMode))
        .presentationDragIndicator(.visible)
        .presentationDetents([.height(contentHeight)])
        .onChange(of: userName) { onClearError() }
        .onChange(of: rating) { onClearError() }
        .onChange(of: comment) { onClearError() }
    }

    // MARK: - Header

    /// Cancel | title, with an invisible Cancel on the right so the title
    /// stays centered (as on Android)
    private var header: some View {
        HStack {
            cancelButton
            Spacer()
            Text(L(\.write_review))
                .font(.headline.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)
            Spacer()
            cancelButton
                .hidden()
                .accessibilityHidden(true)
        }
    }

    private var cancelButton: some View {
        Button(L(\.cancel)) {
            onDismiss()
        }
        .font(.subheadline.weight(.medium))
        .foregroundColor(AppColors.primary(appearance))
        .accessibilityIdentifier("writeReview.cancelButton")
    }

    // MARK: - Fields

    private var nameField: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            fieldLabel(L(\.review_your_name))

            TextField(L(\.review_enter_your_name), text: $userName)
                .textFieldStyle(CustomTextFieldStyle())
                .textContentType(.name)
                .autocapitalization(.words)
                .accessibilityLabel(L(\.review_your_name))
                .accessibilityIdentifier("writeReview.nameField")

            if showFieldErrors && nameMissing {
                errorText(L(\.review_name_required))
            }
        }
    }

    private var ratingPicker: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            fieldLabel(L(\.review_your_rating))

            // 36pt stars in 48pt targets, centered, as on Android
            HStack(spacing: 0) {
                ForEach(1...5, id: \.self) { star in
                    Button {
                        rating = star
                    } label: {
                        Image(systemName: star <= rating ? "star.fill" : "star")
                            .font(.system(size: 30))
                            .foregroundColor(star <= rating
                                ? AppColors.secondary(isDarkMode: isDarkMode)
                                : AppColors.gray)
                            .frame(width: 48, height: 48)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(L(\.review_rate_star, star))
                    .accessibilityAddTraits(star == rating ? .isSelected : [])
                    .accessibilityIdentifier("writeReview.star\(star)")
                }
            }
            .frame(maxWidth: .infinity)

            if showFieldErrors && ratingMissing {
                errorText(L(\.review_select_rating_error))
            }
        }
    }

    private var commentField: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            fieldLabel(L(\.review_your_review))

            ZStack(alignment: .topLeading) {
                TextEditor(text: $comment)
                    .scrollContentBackground(.hidden)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .frame(height: 120)
                    .padding(AppTheme.Spacing.xs)
                    .accessibilityLabel(L(\.review_your_review))
                    .accessibilityIdentifier("writeReview.commentField")

                if comment.isEmpty {
                    Text(L(\.review_share_experience_placeholder))
                        .foregroundColor(AppColors.gray)
                        .padding(.horizontal, AppTheme.Spacing.sm + AppTheme.Spacing.xs)
                        .padding(.vertical, AppTheme.Spacing.md)
                        .allowsHitTesting(false)
                        .accessibilityHidden(true)
                }
            }
            .background(AppColors.surface(isDarkMode: isDarkMode))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(AppColors.gray.opacity(0.3), lineWidth: 1)
            )
            .onChange(of: comment) { _, newValue in
                // Hard cap like Android's dialog, which drops input past 500
                if newValue.count > Self.maxCommentLength {
                    comment = String(newValue.prefix(Self.maxCommentLength))
                }
            }

            Text(L(\.review_character_count, comment.count, Self.maxCommentLength))
                .font(.caption)
                .foregroundColor(AppColors.textSecondary(appearance))
                .frame(maxWidth: .infinity, alignment: .trailing)
                .accessibilityIdentifier("writeReview.characterCount")
        }
    }

    // MARK: - Helpers

    private func fieldLabel(_ text: String) -> some View {
        Text(text)
            .font(.subheadline.weight(.semibold))
            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
    }

    private func errorText(_ text: String) -> some View {
        Text(text)
            .font(.caption)
            .foregroundColor(AppColors.error)
            .fixedSize(horizontal: false, vertical: true)
    }

    private func submit() {
        guard !isSubmitting else { return }
        guard !nameMissing, !ratingMissing else {
            showFieldErrors = true
            return
        }
        showFieldErrors = false
        isSubmitting = true
        Task {
            await onSubmit(userName, rating, comment)
            isSubmitting = false
        }
    }
}
