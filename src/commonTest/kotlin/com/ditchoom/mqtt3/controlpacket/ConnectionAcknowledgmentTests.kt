@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.allocateNewBuffer
import com.ditchoom.mqtt.controlpacket.format.fixed.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ConnectionAcknowledgmentTests {

    @Test
    fun serializeDeserializeDefault() {
        val buffer = allocateNewBuffer(4u)
        val actual = ConnectionAcknowledgment()
        actual.serialize(buffer)
        buffer.resetForRead()
        val expected = ControlPacketV4.from(buffer)
        assertEquals(expected, actual)
    }

    @Test
    fun bit0SessionPresentFalseFlags() {
        val buffer = allocateNewBuffer(4u)
        val model = ConnectionAcknowledgment()
        model.header.serialize(buffer)
        buffer.resetForRead()
        val sessionPresentBit = buffer.readUnsignedByte().get(0)
        assertFalse(sessionPresentBit)

        val buffer2 = allocateNewBuffer(4u)
        model.serialize(buffer2)
        buffer2.resetForRead()
        val result = ControlPacketV4.from(buffer2) as ConnectionAcknowledgment
        assertFalse(result.header.sessionPresent)
    }

    @Test
    fun bit0SessionPresentFlags() {
        val buffer = allocateNewBuffer(4u)
        val model = ConnectionAcknowledgment(ConnectionAcknowledgment.VariableHeader(true))
        model.header.serialize(buffer)
        buffer.resetForRead()
        assertTrue(buffer.readUnsignedByte().get(0))
    }
}