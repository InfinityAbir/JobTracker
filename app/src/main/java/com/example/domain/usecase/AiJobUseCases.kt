package com.example.domain.usecase

import com.example.data.remote.GeminiService
import com.example.data.remote.ParsedJobData
import com.example.domain.model.JobApplication

class ParseJobDescriptionUseCase(private val geminiService: GeminiService) {
    suspend operator fun invoke(rawJobText: String): Result<ParsedJobData> {
        if (rawJobText.isBlank()) {
            return Result.failure(IllegalArgumentException("Job posting text cannot be empty"))
        }
        return geminiService.parseJobPosting(rawJobText)
    }
}

class GenerateCoverLetterUseCase(private val geminiService: GeminiService) {
    suspend operator fun invoke(
        companyName: String,
        jobTitle: String,
        notes: String,
        candidateProfile: String = ""
    ): Result<String> {
        if (companyName.isBlank() || jobTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Company name and Job title are required"))
        }
        return geminiService.generateCoverLetter(
            companyName = companyName,
            jobTitle = jobTitle,
            notesOrDescription = notes,
            userCandidateHighlights = candidateProfile
        )
    }
}

class GenerateFollowUpEmailUseCase(private val geminiService: GeminiService) {
    suspend operator fun invoke(
        application: JobApplication,
        emailType: String
    ): Result<String> {
        if (application.companyName.isBlank() || application.jobTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Company name and Job title are required"))
        }
        return geminiService.generateFollowUpEmail(application, emailType)
    }
}
