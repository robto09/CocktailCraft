import SwiftUI

struct OrderListView: View {
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            VStack {
                Text("Order history will be displayed here")
                    .foregroundColor(.secondary)
                    .padding()
            }
            .navigationTitle("My Orders")
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}