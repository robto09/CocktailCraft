import Foundation
import shared

// Swift-side access to the shared strings (MR.strings / MR.plurals, defined
// once in core/resources/src/commonMain/moko-resources). moko-resources generates a
// Localizable.strings(dict) bundle that the "Compile Kotlin Framework" phase
// copies into the app; these helpers resolve against it for the current
// locale so call sites stay terse:
//
//     Text(L(\.cart_empty_title))
//     L(\.confirm_order_message, total.formatted())
//     L(\.review_count, count: reviews.count)
//
// The results are plain Strings (already localized), so pass them to
// Text(verbatim:)-style APIs — Text(_: String) is fine, it never re-localizes
// a String variable.

/// A shared string, e.g. `L(\.cart_empty_title)`.
func L(_ key: KeyPath<MR.strings, StringResource>) -> String {
    MR.strings.shared[keyPath: key].desc().localized()
}

/// A shared string with positional format arguments (`%1$s`, `%1$d`, …), e.g.
/// `L(\.added_to_cart, cocktail.name)`.
func L(_ key: KeyPath<MR.strings, StringResource>, _ args: Any...) -> String {
    ResourceFormattedStringDesc(stringRes: MR.strings.shared[keyPath: key], args: args).localized()
}

/// A shared plural; `count` picks the form and is also the first format
/// argument unless explicit `args` are given.
func L(_ key: KeyPath<MR.plurals, PluralsResource>, count: Int, _ args: Any...) -> String {
    PluralFormattedStringDesc(
        pluralsRes: MR.plurals.shared[keyPath: key],
        number: Int32(count),
        args: args.isEmpty ? [count] : args
    ).localized()
}
