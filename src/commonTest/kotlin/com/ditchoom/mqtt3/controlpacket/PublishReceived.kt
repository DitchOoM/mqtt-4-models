@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.allocateNewBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class PublishReceivedTests {
    private val packetIdentifier = 2

    @Test
    fun packetIdentifier() {
        val puback = PublishReceived(packetIdentifier)
        assertEquals(4u, puback.packetSize())
        val buffer = allocateNewBuffer(4u)
        puback.serialize(buffer)
        buffer.resetForRead()
        val pubackResult = ControlPacketV4.from(buffer) as PublishReceived
        assertEquals(pubackResult.packetIdentifier, packetIdentifier)
    }

    @Test
    fun packetIdentifierSendDefaults() {
        val puback = PublishReceived(packetIdentifier)
        assertEquals(4u, puback.packetSize())
        val buffer = allocateNewBuffer(4u)
        puback.serialize(buffer)
        buffer.resetForRead()
        val pubackResult = ControlPacketV4.from(buffer) as PublishReceived
        assertEquals(pubackResult.packetIdentifier, packetIdentifier)
    }
}
