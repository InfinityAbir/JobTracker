package com.example.domain.usecase

import com.example.domain.model.JobApplication
import com.example.domain.model.JobSortOption
import com.example.domain.model.JobStatus
import com.example.domain.model.StatusSummary
import com.example.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetJobApplicationsUseCase(private val repository: JobRepository) {
    operator fun invoke(
        searchQuery: String = "",
        statusFilter: JobStatus? = null,
        sortOption: JobSortOption = JobSortOption.DATE_DESC
    ): Flow<List<JobApplication>> {
        return repository.getApplications().map { list ->
            var filtered = list

            if (searchQuery.isNotBlank()) {
                val query = searchQuery.trim().lowercase()
                filtered = filtered.filter { job ->
                    job.companyName.lowercase().contains(query) ||
                    job.jobTitle.lowercase().contains(query) ||
                    job.notes.lowercase().contains(query) ||
                    job.salary.lowercase().contains(query)
                }
            }

            if (statusFilter != null) {
                filtered = filtered.filter { it.status == statusFilter }
            }

            when (sortOption) {
                JobSortOption.DATE_DESC -> filtered.sortedByDescending { it.dateApplied.ifBlank { "0" } }
                JobSortOption.DATE_ASC -> filtered.sortedBy { it.dateApplied.ifBlank { "9" } }
                JobSortOption.COMPANY_ASC -> filtered.sortedBy { it.companyName.lowercase() }
                JobSortOption.TITLE_ASC -> filtered.sortedBy { it.jobTitle.lowercase() }
            }
        }
    }
}

class GetJobByIdUseCase(private val repository: JobRepository) {
    operator fun invoke(id: Long): Flow<JobApplication?> {
        return repository.getApplicationById(id)
    }

    suspend fun getDirect(id: Long): JobApplication? {
        return repository.getApplicationByIdDirect(id)
    }
}

class SaveJobApplicationUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(application: JobApplication): Result<Long> {
        if (application.companyName.isBlank()) {
            return Result.failure(IllegalArgumentException("Company name cannot be empty"))
        }
        if (application.jobTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Job title cannot be empty"))
        }

        return try {
            if (application.id == 0L) {
                val id = repository.insertApplication(application)
                Result.success(id)
            } else {
                repository.updateApplication(application)
                Result.success(application.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/** "Delete" in the UI: moves the application to the trash, where it can still be restored. */
class DeleteJobApplicationUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(application: JobApplication) {
        repository.moveToTrash(application.id)
    }

    suspend operator fun invoke(id: Long) {
        repository.moveToTrash(id)
    }
}

class GetTrashedJobsUseCase(private val repository: JobRepository) {
    operator fun invoke(): Flow<List<JobApplication>> = repository.getTrashedApplications()
}

class RestoreJobFromTrashUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(id: Long) {
        repository.restoreFromTrash(id)
    }
}

/** Irreversible: removes the row from the database. Only the trash screen may call this. */
class DeleteJobPermanentlyUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteApplicationById(id)
    }
}

class EmptyTrashUseCase(private val repository: JobRepository) {
    /** Returns the number of applications permanently deleted. */
    suspend operator fun invoke(): Int = repository.emptyTrash()
}

class GetStatusSummaryUseCase(private val repository: JobRepository) {
    operator fun invoke(): Flow<StatusSummary> {
        return repository.getApplications().map { list ->
            var appliedCount = 0
            var interviewingCount = 0
            var offerCount = 0
            var rejectedCount = 0

            for (job in list) {
                when (job.status) {
                    JobStatus.APPLIED -> appliedCount++
                    JobStatus.INTERVIEWING -> interviewingCount++
                    JobStatus.OFFER -> offerCount++
                    JobStatus.REJECTED -> rejectedCount++
                }
            }

            StatusSummary(
                total = list.size,
                applied = appliedCount,
                interviewing = interviewingCount,
                offer = offerCount,
                rejected = rejectedCount
            )
        }
    }
}
