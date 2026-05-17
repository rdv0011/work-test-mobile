import Foundation
import FirebaseAnalytics
import shared

class FirebaseAnalyticsService: NSObject, AnalyticsService {
    private let queue = DispatchQueue(label: "io.umain.analytics.ios", qos: .background)
    
    func trackEvent(event: AnalyticsEvent) async throws {
        await queue.async {
            let bundle = NSMutableDictionary()
            
            switch event {
            case let screenView as AnalyticsEvent.ScreenView:
                bundle["screen_name"] = screenView.screenName
                bundle["screen_class"] = screenView.screenClass
                if let previousScreen = screenView.previousScreen {
                    bundle["previous_screen"] = previousScreen
                }
                
            case let tabSwitch as AnalyticsEvent.TabSwitch:
                bundle["tab_id"] = tabSwitch.tabId
                bundle["tab_name"] = tabSwitch.tabName
                
            case let modalOpen as AnalyticsEvent.ModalOpen:
                bundle["modal_name"] = modalOpen.modalName
                bundle["modal_class"] = modalOpen.modalClass
                
            case let modalDismiss as AnalyticsEvent.ModalDismiss:
                bundle["modal_name"] = modalDismiss.modalName
                bundle["time_spent_ms"] = NSNumber(value: modalDismiss.timeSpentMs)
                
            case let errorEvent as AnalyticsEvent.ErrorEvent:
                bundle["error_type"] = errorEvent.errorType
                bundle["error_message"] = errorEvent.errorMessage
                bundle["error_context"] = errorEvent.errorContext
                
            case is AnalyticsEvent.CustomEvent:
                break
                
            default:
                break
            }
            
            event.properties.forEach { (key, value) in
                bundle[key] = value
            }
            
            Analytics.logEvent(event.eventName, parameters: bundle as? [String: Any])
        }
    }
    
    func setUserProperties(properties: [String : String]) async throws {
        await queue.async {
            properties.forEach { (key, value) in
                Analytics.setUserProperty(value, forName: key)
            }
        }
    }
    
    func clearUserProperties() async throws {
        await queue.async {
            Analytics.setUserID(nil)
        }
    }
    
    func flush() async throws {
        await queue.async {
            Analytics.resetAnalyticsData()
        }
    }
}
