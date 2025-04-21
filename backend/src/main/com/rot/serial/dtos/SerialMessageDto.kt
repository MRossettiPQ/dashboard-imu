package com.rot.serial.dtos

enum class SerialMessageType(val description: String) {
    DEVICE_INFO_RESPONSE("Device Info Response"),
    DEVICE_INFO_REQUEST("Device Info Request"),
    WIFI_LIST_REQUEST("Wifi list request"),
    WIFI_LIST_RESPONSE("Wifi list response"),
    WIFI_REGISTER_REQUEST("Wifi register"),
}

open class SerialMessageDto<T>(
    var type: SerialMessageType? = null,
    var data: T? = null
) {

    class DeviceInfoRequestDto {
        var sendToMe: Boolean = true
    }

    class DeviceInfoResponseDto {
        var manufacturer: String? = null
        var productName: String? = null
        var descriptor: String? = null
    }

    class WifiListRequestDto {
        var sendToMe: Boolean = true
    }

    class WifiListResponseDto {
        var ssid: String? = null
        var rssi: Int? = null
    }

    class WifiRegisterRequestDto {
        var ssid: String? = null
        var password: String? = null
    }
}

// Start
class SerialDeviceInfoRequestDto : SerialMessageDto<SerialMessageDto.DeviceInfoRequestDto>(type = SerialMessageType.DEVICE_INFO_REQUEST)
class SerialDeviceInfoResponseDto : SerialMessageDto<SerialMessageDto.DeviceInfoResponseDto>(type = SerialMessageType.DEVICE_INFO_RESPONSE)

// Wi-fi list
class SerialWifiListRequestDto : SerialMessageDto<SerialMessageDto.WifiListRequestDto>(type = SerialMessageType.WIFI_LIST_REQUEST)
class SerialWifiListResponseDto : SerialMessageDto<MutableList<SerialMessageDto.WifiListResponseDto>>(type = SerialMessageType.WIFI_LIST_RESPONSE)

// Wi-fi register broker
class SerialWifiRegisterRequestDto : SerialMessageDto<SerialMessageDto.WifiRegisterRequestDto>(type = SerialMessageType.WIFI_REGISTER_REQUEST)
