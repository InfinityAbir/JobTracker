package com.example.domain.repository

import com.example.domain.model.JobApplication
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getApplications(): Flow<List<JobApplication>>
    suspend fun getAllDirect(): List<JobApplication>
    fun getApplicationById(id: Long): Flow<JobApplication?>
    suspend fun getApplicationByIdDirect(id: Long): JobApplication?
    suspend fun insertApplication(application: JobApplication): Long
    suspend fun insertApplications(applications: List<JobApplication>)
    suspend fun updateApplication(application: JobApplication)
    suspend fun deleteApplication(application: JobApplication)
    suspend fun deleteApplicationById(id: Long)
}
