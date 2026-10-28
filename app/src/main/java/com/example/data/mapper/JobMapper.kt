package com.example.data.mapper

import com.example.data.local.JobApplicationEntity
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus

fun JobApplicationEntity.toDomain(): JobApplication {
    return JobApplication(
        id = id,
        companyName = companyName,
        jobTitle = jobTitle,
        dateApplied = dateApplied,
        status = JobStatus.fromString(status),
        notes = notes,
        salary = salary,
        recruiterName = recruiterName,
        recruiterEmail = recruiterEmail,
        recruiterPhone = recruiterPhone,
        referralSource = referralSource,
        interviewDate = interviewDate,
        interviewTime = interviewTime,
        interviewType = interviewType,
        interviewNotes = interviewNotes,
        reminderSet = reminderSet,
        reminderMinutesBefore = reminderMinutesBefore,
        createdAt = createdAt,
        workModel = workModel,
        offerTotalComp = offerTotalComp,
        offerBenefits = offerBenefits,
        offerPros = offerPros,
        offerCons = offerCons,
        prepQuestions = prepQuestions,
        prepStar = prepStar,
        prepNegotiation = prepNegotiation,
        interviewingAt = interviewingAt,
        lastFollowUpAt = lastFollowUpAt
    )
}

fun JobApplication.toEntity(): JobApplicationEntity {
    return JobApplicationEntity(
        id = id,
        companyName = companyName,
        jobTitle = jobTitle,
        dateApplied = dateApplied,
        status = status.name,
        notes = notes,
        salary = salary,
        recruiterName = recruiterName,
        recruiterEmail = recruiterEmail,
        recruiterPhone = recruiterPhone,
        referralSource = referralSource,
        interviewDate = interviewDate,
        interviewTime = interviewTime,
        interviewType = interviewType,
        interviewNotes = interviewNotes,
        reminderSet = reminderSet,
        reminderMinutesBefore = reminderMinutesBefore,
        createdAt = createdAt,
        workModel = workModel,
        offerTotalComp = offerTotalComp,
        offerBenefits = offerBenefits,
        offerPros = offerPros,
        offerCons = offerCons,
        prepQuestions = prepQuestions,
        prepStar = prepStar,
        prepNegotiation = prepNegotiation,
        interviewingAt = interviewingAt,
        lastFollowUpAt = lastFollowUpAt
    )
}
