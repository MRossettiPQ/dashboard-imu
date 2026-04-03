package com.rot.session.services

import com.rot.core.context.ApplicationContext
import com.rot.session.dtos.CreateSessionDto
import com.rot.session.enums.SessionStatus
import com.rot.session.models.Session
import com.rot.user.models.User
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime

@ApplicationScoped
class SessionService {

    fun create(body: CreateSessionDto): Session {
        val ctx = ApplicationContext.context!!
        var session = Session.findByPhysiotherapistId(ctx.user.id!!) ?: Session()

        if (session.patient?.id != body.patientId || session.sessionDate.isAfter(OffsetDateTime.now().minusMinutes(60))) {
            session.delete()
            session = Session()
        }

        if (session.isNewBean) {
            session.status = SessionStatus.CREATED
            session.patient = User.findOrThrowById(body.patientId)
            session.type = body.type
        }

        session.observation = body.observation
        return session.save()
    }

}