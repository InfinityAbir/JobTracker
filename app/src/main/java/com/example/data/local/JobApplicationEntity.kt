package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val jobTitle: String,
    val dateApplied: String,
    val status: String,
    val notes: String = "",
    val salary: String = "",
    val recruiterName: String = "",
    val recruiterEmail: String = "",
    val recruiterPhone: String = "",
    val referralSource: String = "",
    val interviewDate: String = "",
    val interviewTime: String = "",
    val interviewType: String = "",
    val interviewNotes: String = "",
    val reminderSet: Boolean = false,
    val reminderMinutesBefore: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    // --- Added in schema v4 (see JobDatabase.MIGRATION_3_4). Every column needs a DB default. ---
    @ColumnInfo(defaultValue = "")
    val workModel: String = "",
    @ColumnInfo(defaultValue = "")
    val offerTotalComp: String = "",
    @ColumnInfo(defaultValue = "")
    val offerBenefits: String = "",
    @ColumnInfo(defaultValue = "")
    val offerPros: String = "",
    @ColumnInfo(defaultValue = "")
    val offerCons: String = "",
    @ColumnInfo(defaultValue = "")
    val prepQuestions: String = "",
    @ColumnInfo(defaultValue = "")
    val prepStar: String = "",
    @ColumnInfo(defaultValue = "")
    val prepNegotiation: String = "",
    @ColumnInfo(defaultValue = "0")
    val interviewingAt: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val lastFollowUpAt: Long = 0
)
