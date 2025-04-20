package com.rot.wifi.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.UserDto
import com.rot.user.models.User
import com.rot.wifi.dtos.CreateWifiDto
import com.rot.wifi.dtos.WifiDto
import com.rot.wifi.models.Wifi
import io.quarkus.security.Authenticated
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/wifis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class WifiController {

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response? {
        val wifi = Wifi.findOrThrowById(uuid)
        return ResultContent.of().withContent(WifiDto.from(wifi)).build()
    }

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response? {
        val query = Wifi.createQuery()

        return ResultContent.of().withContent(Wifi.fetch(query, page, rpp).transform(WifiDto::from)).build()
    }

    @POST
    @Path("")
    fun save(@Valid body: CreateWifiDto): Response? {
        val wifi = Wifi()
        wifi.ssid = body.ssid
        wifi.encryptAndSetPassword(body.password!!)
        return ResultContent.of().withContent(WifiDto.from(wifi.save())).build()
    }

}