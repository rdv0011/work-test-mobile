import Foundation
import shared
import FirebaseAnalytics

private let tag = "FirebaseAnalyticsService"

class FirebaseAnalyticsService: NSObject, shared.AnalyticsService {
    
    override init() {
        super.init()
        registerWithKotlin()
    }
    
    private func registerWithKotlin() {
        FirebaseAnalyticsFactoryKt.injectSwiftFirebaseAnalyticsService(service: self)
    }
    
     func trackEvent(event: shared.AnalyticsEvent) async throws {
         let parameters = extractParameters(from: event)
         Analytics.logEvent(event.eventName, parameters: parameters)
         logInfo(tag: tag, message: "📊 Logged event: \(event.eventName) with \(parameters.count) parameters")
     }
    
    private func extractParameters(from event: shared.AnalyticsEvent) -> [String: Any] {
        var parameters: [String: Any] = [:]
        
        event.properties.forEach { (key: String, value: String) in
            parameters[key] = value
        }
        
        return parameters
    }
    
     func setUserProperties(properties: [String: String]) async throws {
         for (key, value) in properties {
             Analytics.setUserProperty(value, forName: key)
         }
         logInfo(tag: tag, message: "👤 Set \(properties.count) user properties")
     }
    
     func clearUserProperties() async throws {
         logInfo(tag: tag, message: "⚠️ Firebase Analytics doesn't support clearing all user properties.")
     }
    
     func flush() async throws {
         logInfo(tag: tag, message: "📤 Manual flush requested (Firebase batches automatically)")
     }
}
