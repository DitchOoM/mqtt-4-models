package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.buffer.WriteBuffer
import com.ditchoom.mqtt.controlpacket.ControlPacket.Companion.readMqttUtf8StringNotValidatedSized
import com.ditchoom.mqtt.controlpacket.ControlPacket.Companion.writeMqttUtf8String
import com.ditchoom.mqtt.controlpacket.ISubscribeRequest
import com.ditchoom.mqtt.controlpacket.ISubscription
import com.ditchoom.mqtt.controlpacket.QualityOfService
import com.ditchoom.mqtt.controlpacket.QualityOfService.*
import com.ditchoom.mqtt.controlpacket.format.ReasonCode
import com.ditchoom.mqtt.controlpacket.format.fixed.DirectionOfFlow
import com.ditchoom.mqtt.controlpacket.utf8Length
import com.ditchoom.mqtt.topic.Filter

/**
 * 3.8 SUBSCRIBE - Subscribe request
 *
 * The SUBSCRIBE packet is sent from the Client to the Server to create one or more Subscriptions. Each Subscription
 * registers a Client’s interest in one or more Topics. The Server sends PUBLISH packets to the Client to forward
 * Application Messages that were published to Topics that match these Subscriptions. The SUBSCRIBE packet also
 * specifies (for each Subscription) the maximum QoS with which the Server can send Application Messages to the Client.
 *
 * Bits 3,2,1 and 0 of the Fixed Header of the SUBSCRIBE packet are reserved and MUST be set to 0,0,1 and 0
 * respectively. The Server MUST treat any other value as malformed and close the Network Connection [MQTT-3.8.1-1].
 */
@Parcelize
data class SubscribeRequest(override val packetIdentifier: Int, override val subscriptions: Set<ISubscription>) :
    ControlPacketV4(ISubscribeRequest.controlPacketValue, DirectionOfFlow.CLIENT_TO_SERVER, 0b10), ISubscribeRequest {

    constructor(packetIdentifier: UShort, topic: Filter, qos: QualityOfService)
            : this(packetIdentifier.toInt(), subscriptions = setOf(Subscription(topic.toString(), qos)))

    constructor(packetIdentifier: UShort, topics: List<Filter>, qos: List<QualityOfService>)
            : this(packetIdentifier.toInt(), subscriptions = Subscription.from(topics, qos))

    override fun variableHeader(writeBuffer: WriteBuffer) {
        writeBuffer.write(packetIdentifier.toUShort())
    }

    override fun payload(writeBuffer: WriteBuffer) = Subscription.writeMany(subscriptions, writeBuffer)

    override fun remainingLength() =
        UShort.SIZE_BYTES + Subscription.sizeMany(subscriptions)

    override fun expectedResponse(): SubscribeAcknowledgement {
        val returnCodes = subscriptions.map {
            when (it.maximumQos) {
                AT_MOST_ONCE -> ReasonCode.GRANTED_QOS_0
                AT_LEAST_ONCE -> ReasonCode.GRANTED_QOS_1
                EXACTLY_ONCE -> ReasonCode.GRANTED_QOS_2
            }
        }
        return SubscribeAcknowledgement(packetIdentifier, returnCodes)
    }

    companion object {

        fun from(buffer: ReadBuffer, remaining: Int): SubscribeRequest {
            val packetIdentifier = buffer.readUnsignedShort().toInt()
            val subscriptions = Subscription.fromMany(buffer, remaining - UShort.SIZE_BYTES)
            return SubscribeRequest(packetIdentifier, subscriptions)
        }
    }
}

@Parcelize
data class Subscription(
    override val topicFilter: String,
    /**
     * Bits 0 and 1 of the Subscription Options represent Maximum QoS field. This gives the maximum
     * QoS level at which the Server can send Application Messages to the Client. It is a Protocol
     * Error if the Maximum QoS field has the value 3.
     */
    override val maximumQos: QualityOfService = AT_LEAST_ONCE
) : ISubscription {

    companion object {
        fun fromMany(buffer: ReadBuffer, remaining: Int): Set<Subscription> {
            val subscriptions = HashSet<Subscription>()
            var bytesRead = 0
            while (bytesRead < remaining) {
                val result = from(buffer)
                bytesRead += result.first
                subscriptions.add(result.second)
            }
            return subscriptions
        }

        fun from(buffer: ReadBuffer): Pair<Int, Subscription> {
            val result = buffer.readMqttUtf8StringNotValidatedSized()
            var bytesRead = UShort.SIZE_BYTES + result.first
            val topicFilter = result.second
            val subOptionsInt = buffer.readUnsignedByte().toInt()
            bytesRead++
            val qosBit1 = subOptionsInt.shl(6).shr(7) == 1
            val qosBit0 = subOptionsInt.shl(7).shr(7) == 1
            val qos = QualityOfService.fromBooleans(qosBit1, qosBit0)
            val filter = Filter(topicFilter)
            filter.validate()
            return Pair(bytesRead, Subscription(filter.topicFilter.toString(), qos))
        }

        fun from(topics: List<Filter>, qos: List<QualityOfService>): Set<ISubscription> {
            if (topics.size != qos.size) {
                throw IllegalArgumentException("Non matching topics collection size with the QoS collection size")
            }
            val subscriptions = mutableSetOf<ISubscription>()
            topics.forEachIndexed { index, topic ->
                subscriptions += Subscription(topic.topicFilter.toString(), qos[index])
            }
            return subscriptions
        }

        fun sizeMany(subscriptions: Collection<ISubscription>): Int {
            var size = 0
            subscriptions.forEach {
                size += it.topicFilter.utf8Length() + UShort.SIZE_BYTES + Byte.SIZE_BYTES
            }
            return size
        }

        fun writeMany(subscriptions: Collection<ISubscription>, writeBuffer: WriteBuffer) {
            subscriptions.forEach {
                writeBuffer.writeMqttUtf8String(it.topicFilter)
                writeBuffer.write(it.maximumQos.integerValue)
            }
        }
    }
}
