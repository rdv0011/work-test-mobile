import Foundation
import shared

func stringResource(key: String, _ args: Any...) -> String {
    let kotlinArray = KotlinArray<AnyObject>(size: Int32(args.count)) { index in
        args[Int(truncating: index)] as AnyObject
    }
    return StringResources_iosKt.stringResource(key: key, args: kotlinArray)
}
