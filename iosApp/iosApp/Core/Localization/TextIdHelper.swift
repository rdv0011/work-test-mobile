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
