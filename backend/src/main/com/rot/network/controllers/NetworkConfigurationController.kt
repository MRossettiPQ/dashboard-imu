package com.rot.network.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.network.dtos.NetworkConfigurationDto
import com.rot.network.models.NetworkConfiguration
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/network-configurations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class NetworkConfigurationController {

    @GET
    @Transactional
    @Path("/{uuid}")
    @Operation(
        summary = "Obter configuração de rede",
        description = "Recupera uma configuração de rede específica através do seu UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Configuração de rede encontrada com sucesso"
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Configuração de rede não encontrada")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun get(@RestPath("uuid") uuid: UUID): RestResponse<Content<NetworkConfigurationDto>> {
        val networkConfiguration = NetworkConfiguration.findOrThrowById(uuid)
        return ResultContent.of(networkConfiguration)
            .transform(NetworkConfigurationDto::from)
            .build()
    }

    @GET
    @Transactional
    @Path("/")
    @Operation(
        summary = "Listar configurações de rede",
        description = "Retorna uma lista paginada de todas as configurações de rede cadastradas"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista de configurações de rede recuperada com sucesso"
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<NetworkConfigurationDto>>> {
        val query = NetworkConfiguration.createQuery()

        return ResultContent.of(NetworkConfiguration.fetch(query, page, rpp))
            .transform(NetworkConfigurationDto::from)
            .build()
    }

    @POST
    @Transactional
    @Path("/")
    @Operation(
        summary = "Salvar configuração de rede",
        description = "Cadastra uma nova configuração de rede (SSID e senha criptografada)"
    )
    @APIResponse(
        responseCode = "200",
        description = "Configuração de rede salva com sucesso"
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun save(@Valid body: NetworkConfigurationDto): RestResponse<Content<NetworkConfigurationDto>> {
        val networkConfiguration = NetworkConfiguration()
        networkConfiguration.ssid = body.ssid
        networkConfiguration.encryptAndSetPassword(body.password!!)

        return ResultContent.of(networkConfiguration.save())
            .withStatusCode(Response.Status.CREATED)
            .transform(NetworkConfigurationDto::from)
            .withMessage("Configuração de rede salva com sucesso")
            .build()
    }
}