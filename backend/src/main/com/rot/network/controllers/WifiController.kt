package com.rot.network.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.network.dtos.CreateWifiDto
import com.rot.network.dtos.NetworkConfigurationDto
import com.rot.network.models.NetworkConfiguration
import io.quarkus.security.Authenticated
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
        val networkConfiguration = NetworkConfiguration.findOrThrowById(uuid)
        return ResultContent.of().withContent(NetworkConfigurationDto.from(networkConfiguration)).build()
    }

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response? {
        val query = NetworkConfiguration.createQuery()

        return ResultContent.of().withContent(NetworkConfiguration.fetch(query, page, rpp).transform(NetworkConfigurationDto::from)).build()
    }

    @POST
    @Path("/")
    fun save(@Valid body: CreateWifiDto): Response? {
        val networkConfiguration = NetworkConfiguration()
        networkConfiguration.ssid = body.ssid
        networkConfiguration.encryptAndSetPassword(body.password!!)
        return ResultContent.of().withContent(NetworkConfigurationDto.from(networkConfiguration.save())).build()
    }

    @PATCH
    @Path("/{uuid}")
    fun update(@PathParam("uuid") uuid: UUID, @Valid body: CreateWifiDto): Response? {
        val networkConfiguration = NetworkConfiguration.findOrThrowById(uuid)
        networkConfiguration.ssid = body.ssid
        networkConfiguration.encryptAndSetPassword(body.password!!)
        return ResultContent.of().withContent(NetworkConfigurationDto.from(networkConfiguration.save())).build()
    }


}