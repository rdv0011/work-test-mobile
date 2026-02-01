import Foundation
import shared

func inject<T: AnyObject>(_ type: T.Type) -> T {
    let kClass = KotlinKClass(objCClass: type)
    return KoinModule_iosKt.getKoin().get(clazz: kClass, qualifier: nil) as! T
}

@propertyWrapper
struct Inject<T: AnyObject> {
    let wrappedValue: T
    
    init() {
        self.wrappedValue = inject(T.self)
    }
}
