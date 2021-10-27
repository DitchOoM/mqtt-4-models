@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.allocateNewBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class PublishReleaseTests {
    private val packetIdentifier = 2

    @Test
    fun packetIdentifier() {
        val buffer = allocateNewBuffer(4u)
        val puback = PublishRelease(packetIdentifier)
        puback.serialize(buffer)
        buffer.resetForRead()
        val pubackResult = ControlPacketV4.from(buffer) as PublishRelease
        assertEquals(pubackResult.packetIdentifier, packetIdentifier)
    }
}
