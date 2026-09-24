package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer
import com.example.presentation.ai.AiToolsViewModel
import com.example.presentation.dashboard.DashboardViewModel
import com.example.presentation.detail.AddEditJobViewModel

class JobViewModelFactory(
    private val appContainer: AppContainer,
    private val jobId: Long = 0L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(
                    getJobApplicationsUseCase = appContainer.getJobApplicationsUseCase,
                    getStatusSummaryUseCase = appContainer.getStatusSummaryUseCase,
                    deleteJobApplicationUseCase = appContainer.deleteJobApplicationUseCase,
                    saveJobApplicationUseCase = appContainer.saveJobApplicationUseCase,
                    context = appContainer.context
                ) as T
            }
            modelClass.isAssignableFrom(AddEditJobViewModel::class.java) -> {
                AddEditJobViewModel(
                    jobId = jobId,
                    getJobByIdUseCase = appContainer.getJobByIdUseCase,
                    saveJobApplicationUseCase = appContainer.saveJobApplicationUseCase,
                    deleteJobApplicationUseCase = appContainer.deleteJobApplicationUseCase,
                    context = appContainer.context
                ) as T
            }
            modelClass.isAssignableFrom(AiToolsViewModel::class.java) -> {
                AiToolsViewModel(
                    currentJobId = jobId,
                    getJobByIdUseCase = appContainer.getJobByIdUseCase,
                    parseJobDescriptionUseCase = appContainer.parseJobDescriptionUseCase,
                    generateCoverLetterUseCase = appContainer.generateCoverLetterUseCase,
                    generateFollowUpEmailUseCase = appContainer.generateFollowUpEmailUseCase,
                    saveJobApplicationUseCase = appContainer.saveJobApplicationUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
