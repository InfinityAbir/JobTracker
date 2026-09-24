package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JobApplicationDao {

    @Query("SELECT * FROM job_applications ORDER BY id DESC")
    fun getAllApplications(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications")
    suspend fun getAllDirect(): List<JobApplicationEntity>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    fun getApplicationById(id: Long): Flow<JobApplicationEntity?>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getApplicationByIdDirect(id: Long): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<JobApplicationEntity>): List<Long>

    @Update
    suspend fun updateApplication(application: JobApplicationEntity)

    @Delete
    suspend fun deleteApplication(application: JobApplicationEntity)

    @Query("DELETE FROM job_applications WHERE id = :id")
    suspend fun deleteApplicationById(id: Long)

    @Query("SELECT * FROM job_applications WHERE status = :status ORDER BY id DESC")
    fun getApplicationsByStatus(status: String): Flow<List<JobApplicationEntity>>

    @Query("SELECT COUNT(*) FROM job_applications")
    fun getApplicationCount(): Flow<Int>
}
