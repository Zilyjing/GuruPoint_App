package com.example.gurupoint.data.model

data class AppSystemConfig(
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "GuruPoint platform is currently under scheduled maintenance. Please check back shortly!",
    val isEmergencyGamesStopped: Boolean = false,
    val isRegistrationEnabled: Boolean = true,
    val supportWhatsAppNumber: String = "919999999999",
    val appName: String = "GuruPoint",
    val adminPortalUrl: String = "https://admin.gurupoint.com",
    val agentPortalUrl: String = "https://agent.gurupoint.com"
)
