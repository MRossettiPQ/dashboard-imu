import com.rot.core.utils.JsonUtils
import com.rot.measurement.dtos.MeasurementDto
import com.rot.session.models.Session
import io.quarkus.logging.Log
import io.smallrye.reactive.messaging.MutinyEmitter
import io.smallrye.reactive.messaging.mqtt.MqttMessageMetadata
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import java.util.*
import java.util.concurrent.CompletionStage


private inline fun <reified T> convertPayload(payload: ByteArray): T {
    return JsonUtils.toObject<T>(payload)
}

@ApplicationScoped
class MqttReactiveConsumer {


//    @Channel("prices")
//    var emitter: MutinyEmitter<Double?>? = null

    @Incoming("measurements")
    fun consumeMeasurements(message: Message<ByteArray>): CompletionStage<Void> {
        val payloadBytes = message.payload
        val metadata = message.getMetadata(MqttMessageMetadata::class.java).orElse(null)
        val topic = metadata?.topic ?: ""
        val topicParts = topic.split("/")

        if (topicParts.size >= 3) {
            val sessionIdStr = topicParts[1]

            try {
                val sessionId = UUID.fromString(sessionIdStr)
                val measurements = convertPayload<MutableSet<MeasurementDto>>(payloadBytes)

                Log.info("Recebidas ${measurements.size} medições para a sessão: $sessionId")

                 saveMeasurementsBatch(sessionId, measurements)
            } catch (e: Exception) {
                Log.error("Erro ao converter payload ou extrair ID da sessão", e)
            }
        }

        return message.ack()
    }

    @Transactional
    fun saveMeasurementsBatch(sessionId: UUID, measurements: MutableSet<MeasurementDto>) {
        val session = Session.findOrCreateInstance(sessionId)




        session.save()
    }

}