import Foundation
import shared

func tr(_ key: String, _ args: Any...) -> String {
    return TranslationKt.tr(key: key, args: KotlinArray<AnyObject>(size: Int32(args.count)) { index in
        args[Int(truncating: index)] as AnyObject
    })
}

func getCurrentLocale() -> String {
    return TranslationKt.getCurrentLocale()
}

extension String {
    func localized(_ args: Any...) -> String {
        return tr(self, args)
    }
}
