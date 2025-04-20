import java.io.File
import java.nio.file.Files
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64

fun registerGenerateJwtKeysTask(name: String, targetDir: String) {
    tasks.register(name) {
        group = "build setup"
        description = "Gera chaves RSA para JWT na pasta '$targetDir'"

        val outputDir = File("$projectDir/src/main/resources/META-INF/resources", targetDir)

        doLast {
            if (!outputDir.exists()) outputDir.mkdirs()

            val privateFile = File(outputDir, "private.pem")
            if (!privateFile.exists()) {
                println("🔐 Gerando chaves RSA em ${outputDir.absolutePath}")
                val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                keyPairGenerator.initialize(2048)
                val keyPair = keyPairGenerator.generateKeyPair()
                val privateKey = keyPair.private as RSAPrivateKey
                val publicKey = keyPair.public as RSAPublicKey

                val encoder = Base64.getMimeEncoder(64, "\n".toByteArray())
                val privatePem = buildString {
                    appendLine("-----BEGIN PRIVATE KEY-----")
                    appendLine(encoder.encodeToString(privateKey.encoded))
                    appendLine("-----END PRIVATE KEY-----")
                }
                val publicPem = buildString {
                    appendLine("-----BEGIN PUBLIC KEY-----")
                    appendLine(encoder.encodeToString(publicKey.encoded))
                    appendLine("-----END PUBLIC KEY-----")
                }

                Files.write(outputDir.resolve("private.pem").toPath(), privatePem.toByteArray())
                Files.write(outputDir.resolve("public.pem").toPath(), publicPem.toByteArray())

                println("✅ Chaves salvas em ${outputDir.absolutePath}")
            }
        }
    }
}

// Registra tasks separadas
registerGenerateJwtKeysTask("generateJwtKeysForSecurity", "security")
registerGenerateJwtKeysTask("generateJwtKeysForEncrypt", "encrypt")

tasks.named("processResources") {
    dependsOn("generateJwtKeysForSecurity", "generateJwtKeysForEncrypt")
}
