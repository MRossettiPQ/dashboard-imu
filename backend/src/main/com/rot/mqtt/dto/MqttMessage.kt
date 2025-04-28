package com.rot.mqtt.dto

class MqttMessage<T> {
    var type: String? = null
    var content: T? = null
}