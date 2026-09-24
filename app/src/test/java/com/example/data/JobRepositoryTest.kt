package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.JobApplicationDao
import com.example.data.local.JobApplicationEntity
import com.example.data.repository.JobRepositoryImpl
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class FakeJobApplicationDao : JobApplicationDao {
    val items = MutableStateFlow<List<JobApplicationEntity>>(emptyList())
    var lastInsertedItem: JobApplicationEntity? = null
    var lastDeletedItem: JobApplicationEntity? = null
    var lastDeletedId: Long? = null

    override fun getAllApplications(): Flow<List<JobApplicationEntity>> = items

    override suspend fun getAllDirect(): List<JobApplicationEntity> = items.value

    override fun getApplicationById(id: Long): Flow<JobApplicationEntity?> =
        items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getApplicationByIdDirect(id: Long): JobApplicationEntity? =
        items.value.firstOrNull { it.id == id }

    override suspend fun insertApplication(application: JobApplicationEntity): Long {
        val id = if (application.id == 0L) (items.value.maxOfOrNull { it.id } ?: 0L) + 1L else application.id
        val newEntity = application.copy(id = id)
        lastInsertedItem = newEntity
        items.value = items.value.filter { it.id != id } + newEntity
        return id
    }

    override suspend fun insertApplications(applications: List<JobApplicationEntity>): List<Long> {
        val ids = mutableListOf<Long>()
        applications.forEach { app ->
            ids.add(insertApplication(app))
        }
        return ids
    }

    override suspend fun updateApplication(application: JobApplicationEntity) {
        items.value = items.value.map { if (it.id == application.id) application else it }
    }

    override suspend fun deleteApplication(application: JobApplicationEntity) {
        lastDeletedItem = application
        items.value = items.value.filter { it.id != application.id }
    }

    override suspend fun deleteApplicationById(id: Long) {
        lastDeletedId = id
        items.value = items.value.filter { it.id != id }
    }

    override fun getApplicationsByStatus(status: String): Flow<List<JobApplicationEntity>> =
        items.map { list -> list.filter { it.status == status } }

    override fun getApplicationCount(): Flow<Int> =
        items.map { it.size }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class JobRepositoryTest {

    private lateinit var dao: FakeJobApplicationDao
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: JobRepositoryImpl
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        dao = FakeJobApplicationDao()
        repository = JobRepositoryImpl(context = context, dao = dao, ioDispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getApplications_returnsMappedDomainList() = runTest(testDispatcher) {
        val entity = JobApplicationEntity(
            id = 1L,
            companyName = "Google",
            jobTitle = "Android Engineer",
            dateApplied = "2026-09-20",
            status = "APPLIED",
            salary = "$180,000",
            notes = "Referral via Jane"
        )
        dao.items.value = listOf(entity)

        val result = repository.getApplications().first()

        assertEquals(1, result.size)
        assertEquals("Google", result[0].companyName)
        assertEquals("Android Engineer", result[0].jobTitle)
        assertEquals(JobStatus.APPLIED, result[0].status)
    }

    @Test
    fun insertApplication_callsDaoInsert() = runTest(testDispatcher) {
        val app = JobApplication(
            companyName = "Stripe",
            jobTitle = "Mobile Lead",
            dateApplied = "2026-09-20",
            status = JobStatus.INTERVIEWING
        )

        val insertedId = repository.insertApplication(app)

        assertEquals(1L, insertedId)
        assertNotNull(dao.lastInsertedItem)
        assertEquals("Stripe", dao.lastInsertedItem?.companyName)
    }

    @Test
    fun deleteApplication_callsDaoDelete() = runTest(testDispatcher) {
        val app = JobApplication(
            id = 5L,
            companyName = "Netflix",
            jobTitle = "Software Engineer",
            dateApplied = "2026-09-18"
        )

        repository.deleteApplication(app)

        assertEquals(5L, dao.lastDeletedItem?.id)
        assertEquals("Netflix", dao.lastDeletedItem?.companyName)
    }
}
