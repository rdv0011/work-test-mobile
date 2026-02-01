import Foundation
import shared

func tr(_ key: TextId, _ args: Any...) -> String {
    return TranslationKt.tr(textId: key, args: KotlinArray<AnyObject>(size: Int32(args.count)) { index in
        args[Int(truncating: index)] as AnyObject
    })
}

func getCurrentLocale() -> String {
    return TranslationKt.getCurrentLocale()
}
