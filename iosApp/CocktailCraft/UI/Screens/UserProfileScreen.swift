import SwiftUI
import shared

struct UserProfileScreen: View {
    @StateObject private var viewModel = DependencyContainer.shared.makeUserProfileViewModel()
    @State private var showingAddressSheet = false
    @State private var showingPaymentSheet = false
    @State private var showingImagePicker = false
    @State private var showingError = false
    
    var body: some View {
        NavigationView {
            List {
                if let user = viewModel.user {
                    // Profile Header
                    Section {
                        HStack {
                            AsyncImage(url: URL(string: user.profilePicture ?? "")) { image in
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Image(systemName: "person.circle.fill")
                                    .foregroundColor(.gray)
                            }
                            .frame(width: 80, height: 80)
                            .clipShape(Circle())
                            .onTapGesture {
                                showingImagePicker = true
                            }
                            
                            VStack(alignment: .leading) {
                                Text(user.name)
                                    .font(.headline)
                                Text(user.email)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding(.vertical, 8)
                    }
                    
                    // Personal Information
                    Section(header: Text("Personal Information")) {
                        NavigationLink(destination: EditProfileView(user: user)) {
                            Label("Edit Profile", systemImage: "person.fill")
                        }
                        
                        if let phone = user.phoneNumber {
                            LabeledContent("Phone", value: phone)
                        }
                    }
                    
                    // Addresses
                    Section(header: Text("Delivery Addresses")) {
                        ForEach(user.addresses, id: \.zipCode) { address in
                            AddressRow(
                                address: address,
                                isDefault: address.zipCode == user.preferences.defaultAddress,
                                onSetDefault: {
                                    viewModel.setDefaultAddress(addressId: address.zipCode)
                                },
                                onRemove: {
                                    viewModel.removeAddress(addressId: address.zipCode)
                                }
                            )
                        }
                        
                        Button(action: { showingAddressSheet = true }) {
                            Label("Add Address", systemImage: "plus.circle.fill")
                        }
                    }
                    
                    // Payment Methods
                    Section(header: Text("Payment Methods")) {
                        ForEach(user.paymentMethods, id: \.id) { method in
                            PaymentMethodRow(
                                method: method,
                                isDefault: method.id == user.preferences.defaultPaymentMethod,
                                onSetDefault: {
                                    viewModel.setDefaultPaymentMethod(methodId: method.id)
                                },
                                onRemove: {
                                    viewModel.removePaymentMethod(paymentMethodId: method.id)
                                }
                            )
                        }
                        
                        Button(action: { showingPaymentSheet = true }) {
                            Label("Add Payment Method", systemImage: "plus.circle.fill")
                        }
                    }
                    
                    // Preferences
                    Section(header: Text("Preferences")) {
                        Toggle("Notifications", isOn: Binding(
                            get: { user.preferences.notificationsEnabled },
                            set: { viewModel.toggleNotifications(enabled: $0) }
                        ))
                        
                        Toggle("Email Updates", isOn: Binding(
                            get: { user.preferences.emailNotificationsEnabled },
                            set: { viewModel.toggleEmailNotifications(enabled: $0) }
                        ))
                        
                        Toggle("SMS Updates", isOn: Binding(
                            get: { user.preferences.smsNotificationsEnabled },
                            set: { viewModel.toggleSmsNotifications(enabled: $0) }
                        ))
                        
                        Picker("Theme", selection: Binding(
                            get: { user.preferences.theme },
                            set: { viewModel.updateTheme(theme: $0) }
                        )) {
                            Text("Light").tag(Theme.LIGHT)
                            Text("Dark").tag(Theme.DARK)
                            Text("System").tag(Theme.SYSTEM)
                        }
                    }
                    
                    // Account Actions
                    Section {
                        Button(role: .destructive, action: {
                            // TODO: Implement logout
                        }) {
                            Label("Sign Out", systemImage: "arrow.right.doc.on.clipboard")
                        }
                        
                        Button(role: .destructive, action: {
                            // TODO: Implement account deletion
                        }) {
                            Label("Delete Account", systemImage: "trash.fill")
                        }
                    }
                }
            }
            .navigationTitle("Profile")
            .refreshable {
                // Refresh user data
            }
            .alert("Error", isPresented: $showingError, presenting: viewModel.error) { _ in
                Button("OK") {}
            } message: { error in
                Text(error)
            }
            .sheet(isPresented: $showingAddressSheet) {
                AddAddressSheet { address in
                    viewModel.addAddress(address: address)
                }
            }
            .sheet(isPresented: $showingPaymentSheet) {
                AddPaymentMethodSheet { method in
                    viewModel.addPaymentMethod(paymentMethod: method)
                }
            }
        }
    }
}

private struct AddressRow: View {
    let address: DeliveryAddress
    let isDefault: Bool
    let onSetDefault: () -> Void
    let onRemove: () -> Void
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                VStack(alignment: .leading) {
                    Text("\(address.street)")
                        .font(.headline)
                    Text("\(address.city), \(address.state) \(address.zipCode)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                if isDefault {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.accentColor)
                }
            }
            
            HStack {
                if !isDefault {
                    Button("Set as Default") {
                        onSetDefault()
                    }
                    .font(.caption)
                }
                
                Spacer()
                
                Button(role: .destructive, action: onRemove) {
                    Text("Remove")
                        .font(.caption)
                }
            }
        }
    }
}

private struct PaymentMethodRow: View {
    let method: PaymentMethod
    let isDefault: Bool
    let onSetDefault: () -> Void
    let onRemove: () -> Void
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Image(systemName: method.type == .CREDIT_CARD ? "creditcard.fill" : "applepay")
                
                VStack(alignment: .leading) {
                    Text(method.type.name)
                        .font(.headline)
                    if let lastFour = method.lastFourDigits {
                        Text("••••\(lastFour)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
                
                Spacer()
                
                if isDefault {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.accentColor)
                }
            }
            
            HStack {
                if !isDefault {
                    Button("Set as Default") {
                        onSetDefault()
                    }
                    .font(.caption)
                }
                
                Spacer()
                
                Button(role: .destructive, action: onRemove) {
                    Text("Remove")
                        .font(.caption)
                }
            }
        }
    }
}

private struct AddAddressSheet: View {
    let onSubmit: (DeliveryAddress) -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var street = ""
    @State private var city = ""
    @State private var state = ""
    @State private var zipCode = ""
    @State private var country = "USA"
    @State private var additionalInstructions = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Address Details")) {
                    TextField("Street", text: $street)
                    TextField("City", text: $city)
                    TextField("State", text: $state)
                    TextField("ZIP Code", text: $zipCode)
                        .keyboardType(.numberPad)
                    TextField("Additional Instructions", text: $additionalInstructions)
                }
                
                Section {
                    Button(action: submit) {
                        Text("Add Address")
                            .frame(maxWidth: .infinity)
                            .multilineTextAlignment(.center)
                    }
                    .disabled(!isValidForm)
                }
            }
            .navigationTitle("Add Address")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                }
            )
        }
    }
    
    private var isValidForm: Bool {
        !street.isEmpty && !city.isEmpty && !state.isEmpty && !zipCode.isEmpty
    }
    
    private func submit() {
        let address = DeliveryAddress(
            street: street,
            city: city,
            state: state,
            zipCode: zipCode,
            country: country,
            additionalInstructions: additionalInstructions.isEmpty ? nil : additionalInstructions
        )
        onSubmit(address)
        dismiss()
    }
}

private struct AddPaymentMethodSheet: View {
    let onSubmit: (PaymentMethod) -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var cardNumber = ""
    @State private var expiryDate = ""
    @State private var cvv = ""
    @State private var cardHolderName = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Card Details")) {
                    TextField("Card Number", text: $cardNumber)
                        .keyboardType(.numberPad)
                    TextField("MM/YY", text: $expiryDate)
                        .keyboardType(.numberPad)
                    TextField("CVV", text: $cvv)
                        .keyboardType(.numberPad)
                    TextField("Cardholder Name", text: $cardHolderName)
                }
                
                Section {
                    Button(action: submit) {
                        Text("Add Card")
                            .frame(maxWidth: .infinity)
                            .multilineTextAlignment(.center)
                    }
                    .disabled(!isValidForm)
                }
            }
            .navigationTitle("Add Payment Method")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                }
            )
        }
    }
    
    private var isValidForm: Bool {
        cardNumber.count == 16 && expiryDate.count == 5 && cvv.count == 3 && !cardHolderName.isEmpty
    }
    
    private func submit() {
        // TODO: Implement proper payment method creation
        let method = PaymentMethod.CREDIT_CARD
        onSubmit(method)
        dismiss()
    }
}