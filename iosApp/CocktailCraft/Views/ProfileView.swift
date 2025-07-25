import SwiftUI

import shared

struct ProfileView: View {
    @StateObject private var viewModel = ProfileViewModelSKIE()
    @State private var showingSettings = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Profile Header
                VStack(spacing: 16) {
                    Image(systemName: "person.circle.fill")
                        .resizable()
                        .frame(width: 100, height: 100)
                        .foregroundColor(.gray)
                    
                    if viewModel.isLoggedIn {
                        Text(viewModel.userName)
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text(viewModel.userEmail)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    } else {
                        Text("Guest User")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Button(action: {
                            Task {
                                await viewModel.signIn(email: "demo@example.com", password: "password")
                            }
                        }) {
                            Text("Sign In")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 8)
                                .background(Color.blue)
                                .cornerRadius(20)
                        }
                    }
                }
                .padding(.vertical, 32)
                
                // Menu Items
                List {
                    Section {
                        Button(action: { showingSettings = true }) {
                            HStack {
                                Image(systemName: "gearshape.fill")
                                    .foregroundColor(.blue)
                                    .frame(width: 30)
                                Text("Settings")
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .foregroundColor(.gray)
                            }
                        }
                        .foregroundColor(.primary)
                    }
                    
                    if viewModel.isLoggedIn {
                        Section {
                            Button(action: {
                                Task {
                                    await viewModel.signOut()
                                }
                            }) {
                                HStack {
                                    Image(systemName: "arrow.right.square.fill")
                                        .foregroundColor(.red)
                                        .frame(width: 30)
                                    Text("Sign Out")
                                        .foregroundColor(.red)
                                }
                            }
                        }
                    }
                }
                .listStyle(InsetGroupedListStyle())
            }
            .navigationTitle("Profile")
            .sheet(isPresented: $showingSettings) {
                SettingsView()
            }
        }
    }
}