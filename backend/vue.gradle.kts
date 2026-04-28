import org.gradle.api.file.DuplicatesStrategy

// Defina o caminho para o diretório do frontend Vue
val vueProjectDir = file("../frontend")
val vueBuildDir = file("$vueProjectDir/dist")

// Defina a tarefa para executar o build do frontend
val buildVueFrontend by tasks.registering(Exec::class) {
    // A tarefa só será executada no perfil de produção
    onlyIf { project.hasProperty("prod") }

    workingDir = vueProjectDir
    commandLine("npm", "install")
    commandLine("npm", "run", "build")
}

// Tarefa para copiar os arquivos gerados para os recursos do Quarkus
val copyFrontendToQuarkus by tasks.registering(Copy::class) {
    dependsOn(buildVueFrontend) // Só será executado após o build do frontend
    from(vueBuildDir)
    into("src/main/resources/META-INF/resources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Integra com o processo de recursos do Quarkus
tasks.named("processResources") {
    dependsOn(copyFrontendToQuarkus)
}