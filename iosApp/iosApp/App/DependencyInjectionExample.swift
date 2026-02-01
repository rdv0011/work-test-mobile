import SwiftUI
import shared

struct DependencyInjectionExample: View {
    @StateObject private var viewModel = ExampleViewModel()
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Dependency Injection Example")
                .font(.title)
            
            Text("Coordinator: \(viewModel.coordinatorInfo)")
            Text("Network Service: \(viewModel.networkServiceInfo)")
            Text("Translation Service: \(viewModel.translationServiceInfo)")
            
            Button("Test All Services") {
                viewModel.testServices()
            }
            .padding()
        }
        .padding()
    }
}

class ExampleViewModel: ObservableObject {
    private let koin = KoinModuleKt.getKoin()
    
    @Published var coordinatorInfo = ""
    @Published var networkServiceInfo = ""
    @Published var translationServiceInfo = ""
    
    init() {
        loadServiceInfo()
    }
    
    func loadServiceInfo() {
        let coordinator = koin.get(objCClass: AppCoordinator.self) as! AppCoordinator
        coordinatorInfo = "✓ Coordinator loaded"
        
        let networkService = koin.get(objCClass: NetworkService.self) as! NetworkService
        networkServiceInfo = "✓ NetworkService loaded"
        
        let translationService = koin.get(objCClass: TranslationService.self) as! TranslationService
        let locale = translationService.getCurrentLocale()
        translationServiceInfo = "✓ TranslationService (\(locale))"
    }
    
    func testServices() {
        logInfo(tag: "Example", message: "Testing all services")
        
        let appTitle = tr("app.title")
        logInfo(tag: "Example", message: "App title: \(appTitle)")
        
        let listTitle = tr("restaurant.list.title")
        logInfo(tag: "Example", message: "List title: \(listTitle)")
        
        logInfo(tag: "Example", message: "All services tested successfully!")
    }
}
