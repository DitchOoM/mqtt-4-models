@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.mqtt.controlpacket.ControlPacketFactory
import com.ditchoom.mqtt.controlpacket.IPublishMessage
import com.ditchoom.mqtt.controlpacket.QualityOfService

object ControlPacketV4Factory : ControlPacketFactory {
    override fun from(buffer: ReadBuffer, byte1: UByte, remainingLength: UInt) =
        ControlPacketV4.from(buffer, byte1, remainingLength)

    override fun pingRequest() = PingRequest
    override fun pingResponse() = PingResponse

    override fun publish(
        dup: Boolean,
        qos: QualityOfService,
        packetIdentifier: Int?,
        retain: Boolean,
        topicName: CharSequence,
        payload: PlatformBuffer?,
        // MQTT 5 Properties, Should be ignored in this version
        payloadFormatIndicator: Boolean,
        messageExpiryInterval: Long?,
        topicAlias: Int?,
        responseTopic: CharSequence?,
        correlationData: PlatformBuffer?,
        userProperty: List<Pair<CharSequence, CharSequence>>,
        subscriptionIdentifier: Set<Long>,
        contentType: CharSequence?
    ): IPublishMessage {
        val fixedHeader = PublishMessage.FixedHeader(dup, qos, retain)
        val variableHeader = PublishMessage.VariableHeader(topicName, packetIdentifier)
        return PublishMessage(fixedHeader, variableHeader, payload)
    }
}