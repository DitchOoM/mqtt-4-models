package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.mqtt.controlpacket.*
import com.ditchoom.mqtt.controlpacket.format.ReasonCode
import com.ditchoom.mqtt.topic.Filter

@Parcelize
object ControlPacketV4Factory : ControlPacketFactory {

    override fun from(buffer: ReadBuffer, byte1: UByte, remainingLength: Int) =
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

    override fun subscribe(
        packetIdentifier: Int,
        topicFilter: CharSequence,
        maximumQos: QualityOfService,
        noLocal: Boolean,
        retainAsPublished: Boolean,
        retainHandling: ISubscription.RetainHandling,
        serverReference: CharSequence?,
        userProperty: List<Pair<CharSequence, CharSequence>>
    ): ISubscribeRequest {
        val subscription = Subscription(Filter(topicFilter).topicFilter.toString(), maximumQos)
        return subscribe(
            packetIdentifier,
            setOf(subscription),
            serverReference,
            userProperty
        )
    }

    override fun subscribe(
        packetIdentifier: Int,
        subscriptions: Set<ISubscription>,
        serverReference: CharSequence?,
        userProperty: List<Pair<CharSequence, CharSequence>>
    ) = SubscribeRequest(packetIdentifier, subscriptions)

    override fun unsubscribe(
        packetIdentifier: Int,
        topics: Set<CharSequence>,
        userProperty: List<Pair<CharSequence, CharSequence>>
    ) = UnsubscribeRequest(packetIdentifier, topics)

    override fun disconnect(
        reasonCode: ReasonCode,
        sessionExpiryIntervalSeconds: Long?,
        reasonString: CharSequence?,
        userProperty: List<Pair<CharSequence, CharSequence>>
    ) = DisconnectNotification
}