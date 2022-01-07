@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.allocateNewBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsubscribeRequestTests {
    private val packetIdentifier = 2

    @Test
    fun basicTest() {
        val buffer = allocateNewBuffer(17u)
        val unsub = UnsubscribeRequest(packetIdentifier, setOf("yolo", "yolo1"))
        unsub.serialize(buffer)
        buffer.resetForRead()
        val result = ControlPacketV4.from(buffer) as UnsubscribeRequest
        val topics = result.topics.sortedBy { it.toString() }
        assertEquals(topics.first(), "yolo")
        assertEquals(topics[1], "yolo1")
    }
}
