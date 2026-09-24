package com.example.data.repository

import android.content.Context
import com.example.data.local.JobApplicationDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.JobApplication
import com.example.domain.repository.JobRepository
import com.example.util.LocalAutoBackupManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class JobRepositoryImpl(
    private val context: Context,
    private val dao: JobApplicationDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : JobRepository {

    private suspend fun syncBackup() {
        try {
            val all = dao.getAllDirect().map { it.toDomain() }
            LocalAutoBackupManager.saveAutoBackup(context, all)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getApplications(): Flow<List<JobApplication>> {
        return dao.getAllApplications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getAllDirect(): List<JobApplication> = withContext(ioDispatcher) {
        dao.getAllDirect().map { it.toDomain() }
    }

    override fun getApplicationById(id: Long): Flow<JobApplication?> {
        return dao.getApplicationById(id).map { it?.toDomain() }
    }

    override suspend fun getApplicationByIdDirect(id: Long): JobApplication? = withContext(ioDispatcher) {
        dao.getApplicationByIdDirect(id)?.toDomain()
    }

    override suspend fun insertApplication(application: JobApplication): Long = withContext(ioDispatcher) {
        val id = dao.insertApplication(application.toEntity())
        syncBackup()
        id
    }

    override suspend fun insertApplications(applications: List<JobApplication>) = withContext(ioDispatcher) {
        dao.insertApplications(applications.map { it.toEntity() })
        syncBackup()
    }

    override suspend fun updateApplication(application: JobApplication) = withContext(ioDispatcher) {
        dao.updateApplication(application.toEntity())
        syncBackup()
    }

    override suspend fun deleteApplication(application: JobApplication) = withContext(ioDispatcher) {
        dao.deleteApplication(application.toEntity())
        syncBackup()
    }

    override suspend fun deleteApplicationById(id: Long) = withContext(ioDispatcher) {
        dao.deleteApplicationById(id)
        syncBackup()
    }
}
