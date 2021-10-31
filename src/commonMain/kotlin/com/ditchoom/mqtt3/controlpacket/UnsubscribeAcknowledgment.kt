@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.buffer.WriteBuffer
import com.ditchoom.mqtt.controlpacket.IUnsubscribeAcknowledgment
import com.ditchoom.mqtt.controlpacket.format.fixed.DirectionOfFlow

data class UnsubscribeAcknowledgment(override val packetIdentifier: Int) :
    ControlPacketV4(11, DirectionOfFlow.SERVER_TO_CLIENT), IUnsubscribeAcknowledgment {
    override fun variableHeader(writeBuffer: WriteBuffer) {
        writeBuffer.write(packetIdentifier.toUShort())
    }

    companion object {
        fun from(buffer: ReadBuffer) = UnsubscribeAcknowledgment(buffer.readUnsignedShort().toInt())
    }
}
