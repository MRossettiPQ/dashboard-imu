import java.io.File
import java.nio.file.Files
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64

val pemDir = File("$projectDir/src/main/resources/META-INF")

tasks.register("generateJwtKeys") {
    group = "build setup"
    description = "Gera chaves RSA para JWT (private.pem e public.pem)"

    doLast {
        println("Gera chaves RSA para JWT (private.pem e public.pem)")
        if (!pemDir.exists()) {
            pemDir.mkdirs()
        }

        val privateFile = File(pemDir, "private.pem")
        if (!privateFile.exists()) {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            val keyPair = keyPairGenerator.generateKeyPair()
            val privateKey = keyPair.private as RSAPrivateKey
            val publicKey = keyPair.public as RSAPublicKey

            // PEM encode (base64 com cabeçalhos)
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

            Files.write(pemDir.resolve("private.pem").toPath(), privatePem.toByteArray())
            Files.write(pemDir.resolve("public.pem").toPath(), publicPem.toByteArray())

            println("✅ Chaves geradas em: ${pemDir.absolutePath}")
        }
    }
}

tasks.named("processResources") {
    dependsOn("generateJwtKeys")
}