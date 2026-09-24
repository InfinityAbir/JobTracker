package com.example.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddJob : Screen("add_job")
    data object EditJob : Screen("edit_job/{jobId}") {
        fun createRoute(jobId: Long) = "edit_job/$jobId"
    }
}
