package com.example.di

import android.content.Context
import com.example.data.local.JobDatabase
import com.example.data.remote.GeminiService
import com.example.data.repository.JobRepositoryImpl
import com.example.domain.repository.JobRepository
import com.example.domain.usecase.DeleteJobApplicationUseCase
import com.example.domain.usecase.GenerateCoverLetterUseCase
import com.example.domain.usecase.GenerateFollowUpEmailUseCase
import com.example.domain.usecase.GetJobApplicationsUseCase
import com.example.domain.usecase.GetJobByIdUseCase
import com.example.domain.usecase.GetStatusSummaryUseCase
import com.example.domain.usecase.ParseJobDescriptionUseCase
import com.example.domain.usecase.SaveJobApplicationUseCase

interface AppContainer {
    val context: Context
    val database: JobDatabase
    val jobRepository: JobRepository
    val getJobApplicationsUseCase: GetJobApplicationsUseCase
    val getJobByIdUseCase: GetJobByIdUseCase
    val saveJobApplicationUseCase: SaveJobApplicationUseCase
    val deleteJobApplicationUseCase: DeleteJobApplicationUseCase
    val getStatusSummaryUseCase: GetStatusSummaryUseCase
    val parseJobDescriptionUseCase: ParseJobDescriptionUseCase
    val generateCoverLetterUseCase: GenerateCoverLetterUseCase
    val generateFollowUpEmailUseCase: GenerateFollowUpEmailUseCase
}

class DefaultAppContainer(override val context: Context) : AppContainer {

    override val database: JobDatabase by lazy {
        JobDatabase.getInstance(context)
    }

    private val geminiService: GeminiService by lazy {
        GeminiService()
    }

    override val jobRepository: JobRepository by lazy {
        JobRepositoryImpl(context, database.jobApplicationDao())
    }

    override val getJobApplicationsUseCase: GetJobApplicationsUseCase by lazy {
        GetJobApplicationsUseCase(jobRepository)
    }

    override val getJobByIdUseCase: GetJobByIdUseCase by lazy {
        GetJobByIdUseCase(jobRepository)
    }

    override val saveJobApplicationUseCase: SaveJobApplicationUseCase by lazy {
        SaveJobApplicationUseCase(jobRepository)
    }

    override val deleteJobApplicationUseCase: DeleteJobApplicationUseCase by lazy {
        DeleteJobApplicationUseCase(jobRepository)
    }

    override val getStatusSummaryUseCase: GetStatusSummaryUseCase by lazy {
        GetStatusSummaryUseCase(jobRepository)
    }

    override val parseJobDescriptionUseCase: ParseJobDescriptionUseCase by lazy {
        ParseJobDescriptionUseCase(geminiService)
    }

    override val generateCoverLetterUseCase: GenerateCoverLetterUseCase by lazy {
        GenerateCoverLetterUseCase(geminiService)
    }

    override val generateFollowUpEmailUseCase: GenerateFollowUpEmailUseCase by lazy {
        GenerateFollowUpEmailUseCase(geminiService)
    }
}

