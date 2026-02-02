import Foundation
import shared

func tr(_ textId: TextId, _ args: Any...) -> String {
    return TranslationKt.tr(textId: textId, args: KotlinArray<AnyObject>(size: Int32(args.count)) { index in
        args[Int(truncating: index)] as AnyObject
    })
}

extension TextId {
    var localized: String {
        return tr(self)
    }
}

extension TextId {
    static var appTitle: TextId { TextId.AppTitle() }
    static var restaurantListTitle: TextId { TextId.RestaurantListTitle() }
    static var restaurantDetailTitle: TextId { TextId.RestaurantDetailTitle() }
    static var filterAll: TextId { TextId.FilterAll() }
    static var restaurantStatusOpen: TextId { TextId.RestaurantStatusOpen() }
    static var restaurantStatusClosed: TextId { TextId.RestaurantStatusClosed() }
    static var accessibilityRestaurantCard: TextId { TextId.AccessibilityRestaurantCard() }
    static var accessibilityFilterChip: TextId { TextId.AccessibilityFilterChip() }
    static var accessibilityFilterSelected: TextId { TextId.AccessibilityFilterSelected() }
    static var accessibilityBackButton: TextId { TextId.AccessibilityBackButton() }
    static var errorLoading: TextId { TextId.ErrorLoading() }
    static var errorNetwork: TextId { TextId.ErrorNetwork() }
    static var loading: TextId { TextId.Loading() }
}
