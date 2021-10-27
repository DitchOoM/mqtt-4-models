@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.buffer.WriteBuffer
import com.ditchoom.mqtt.controlpacket.IPublishAcknowledgment
import com.ditchoom.mqtt.controlpacket.format.fixed.DirectionOfFlow

/**
 * 3.4 PUBACK – Publish acknowledgement
 *
 * A PUBACK packet is the response to a PUBLISH packet with QoS 1.
 */
data class PublishAcknowledgment(override val packetIdentifier: Int) :
    ControlPacketV4(4, DirectionOfFlow.BIDIRECTIONAL), IPublishAcknowledgment {
    override fun variableHeader(writeBuffer: WriteBuffer) {
        writeBuffer.write(packetIdentifier.toUShort())
    }

    companion object {
        fun from(buffer: ReadBuffer) = PublishAcknowledgment(buffer.readUnsignedShort().toInt())
    }
}
