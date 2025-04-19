//package com.rot.core.providers
//
//import io.quarkus.vertx.web.RouteFilter
//import io.vertx.ext.web.RoutingContext
//import java.nio.file.Files
//import java.nio.file.Paths
//
//class SpaRouteFilter {
//    @RouteFilter(1000) // Prioridade baixa para vir depois dos arquivos estáticos
//    fun routeFilter(rc: RoutingContext) {
//        val path = rc.normalizedPath()
//
//        val isApiRequest = path.startsWith("/api") // Ajuste se seu backend usa outro prefixo
//        val isStaticAsset = Files.exists(Paths.get("src/main/resources/META-INF/resources$path"))
//
//        if (!isApiRequest && !isStaticAsset && !path.contains(".")) {
//            rc.reroute("/")
//        }
//    }
//}