package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.allocate
import com.ditchoom.buffer.toBuffer
import com.ditchoom.mqtt.MalformedPacketException
import com.ditchoom.mqtt.controlpacket.ControlPacket.Companion.readVariableByteInteger
import com.ditchoom.mqtt.controlpacket.QualityOfService
import com.ditchoom.mqtt.controlpacket.format.fixed.get
import com.ditchoom.mqtt3.controlpacket.PublishMessage.FixedHeader
import com.ditchoom.mqtt3.controlpacket.PublishMessage.VariableHeader
import kotlin.test.*

class PublishMessageTests {

    @Test
    fun qosBothBitsSetTo1ThrowsMalformedPacketException() {
        val byte1 = 0b00111110.toByte()
        val remainingLength = 1.toByte()
        val buffer = PlatformBuffer.allocate(2)
        buffer.write(byte1)
        buffer.write(remainingLength)
        buffer.resetForRead()
        try {
            ControlPacketV4.from(buffer)
            fail()
        } catch (e: MalformedPacketException) {
        }
    }

    @Test
    fun qos0AndPacketIdentifierThrowsIllegalArgumentException() {
        val fixed = FixedHeader(qos = QualityOfService.AT_MOST_ONCE)
        val variable = VariableHeader(("t"), 2)
        try {
            PublishMessage(fixed, variable)
            fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun qos1WithoutPacketIdentifierThrowsIllegalArgumentException() {
        val fixed = FixedHeader(qos = QualityOfService.AT_LEAST_ONCE)
        val variable = VariableHeader(("t"))
        try {
            PublishMessage(fixed, variable)
            fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun qos2WithoutPacketIdentifierThrowsIllegalArgumentException() {
        val fixed = FixedHeader(qos = QualityOfService.EXACTLY_ONCE)
        val variable = VariableHeader(("t"))
        try {
            PublishMessage(fixed, variable)
            fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun genericSerialization() {
        val publishMessage = PublishMessage.buildPayload(topicName = "user/log", payload = "yolo".toBuffer())
        val buffer = PlatformBuffer.allocate(16)
        publishMessage.serialize(buffer)
        val publishPayload = publishMessage.payload
        publishPayload?.position(0)
        buffer.resetForRead()
        val firstByte = buffer.readUnsignedByte()
        assertEquals(firstByte.toInt().shr(4), 3, "fixed header control packet type")
        assertFalse(firstByte.get(3), "fixed header publish dup flag")
        assertFalse(firstByte.get(2), "fixed header qos bit 2")
        assertFalse(firstByte.get(1), "fixed header qos bit 1")
        assertFalse(firstByte.get(0), "fixed header retain flag")
        assertEquals(buffer.readVariableByteInteger(), 14, "fixed header remaining length")
        assertEquals(8u, buffer.readUnsignedShort(), "variable header topic name length")
        assertEquals("user/log", buffer.readUtf8(8u).toString(), "variable header topic name value")
        if (publishMessage.variable.packetIdentifier != null) {
            assertEquals(buffer.readUnsignedShort().toInt(), publishMessage.variable.packetIdentifier)
        }
        assertEquals("yolo", buffer.readUtf8(4u).toString(), "payload value")
        buffer.resetForRead()
        val byte1 = buffer.readUnsignedByte()
        val remainingLength = buffer.readVariableByteInteger()
        val result = PublishMessage.from(buffer, byte1, remainingLength)
        assertEquals(publishMessage, result)
    }

    @Test
    fun genericSerializationPublishDupFlag() {
        val publishMessage =
            PublishMessage.buildPayload(topicName = "user/log", payload = "yolo".toBuffer(), dup = true)
        val buffer = PlatformBuffer.allocate(16)
        publishMessage.serialize(buffer)
        publishMessage.payload?.position(0)
        buffer.resetForRead()
        val firstByte = buffer.readUnsignedByte()
        assertEquals(firstByte.toInt().shr(4), 3, "fixed header control packet type")
        assertTrue(firstByte.get(3), "fixed header publish dup flag")
        assertFalse(firstByte.get(2), "fixed header qos bit 2")
        assertFalse(firstByte.get(1), "fixed header qos bit 1")
        assertFalse(firstByte.get(0), "fixed header retain flag")
        assertEquals(buffer.readVariableByteInteger(), 14, "fixed header remaining length")
        assertEquals(8u, buffer.readUnsignedShort(), "variable header topic name length")
        assertEquals("user/log", buffer.readUtf8(8u).toString(), "variable header topic name value")
        if (publishMessage.variable.packetIdentifier != null) {
            assertEquals(buffer.readUnsignedShort().toInt(), publishMessage.variable.packetIdentifier)
        }
        assertEquals("yolo", buffer.readUtf8(4u).toString(), "payload value")
        buffer.resetForRead()
        val byte1 = buffer.readUnsignedByte()
        val remainingLength = buffer.readVariableByteInteger()
        val result = PublishMessage.from(buffer, byte1, remainingLength)
        assertEquals(publishMessage, result)
    }

    @Test
    fun genericSerializationPublishQos1() {
        val publishMessage = PublishMessage.buildPayload(
            topicName = "user/log",
            payload = "yolo".toBuffer(),
            qos = QualityOfService.AT_LEAST_ONCE,
            packetIdentifier = 13
        )
        val buffer = PlatformBuffer.allocate(18)
        publishMessage.serialize(buffer)
        publishMessage.payload?.position(0)
        buffer.resetForRead()
        val firstByte = buffer.readUnsignedByte()
        assertEquals(firstByte.toInt().shr(4), 3, "fixed header control packet type")
        assertFalse(firstByte.get(3), "fixed header publish dup flag")
        assertFalse(firstByte.get(2), "fixed header qos bit 2")
        assertTrue(firstByte.get(1), "fixed header qos bit 1")
        assertFalse(firstByte.get(0), "fixed header retain flag")
        assertEquals(buffer.readVariableByteInteger(), 16, "fixed header remaining length")
        assertEquals(8u, buffer.readUnsignedShort(), "variable header topic name length")
        assertEquals("user/log", buffer.readUtf8(8u).toString(), "variable header topic name value")
        if (publishMessage.variable.packetIdentifier != null) {
            assertEquals(buffer.readUnsignedShort().toInt(), publishMessage.variable.packetIdentifier)
        }
        assertEquals("yolo", buffer.readUtf8(4u).toString(), "payload value")
        buffer.resetForRead()
        val byte1 = buffer.readUnsignedByte()
        val remainingLength = buffer.readVariableByteInteger()
        val result = PublishMessage.from(buffer, byte1, remainingLength)
        assertEquals(publishMessage, result)
    }

    @Test
    fun genericSerializationPublishQos2() {
        val publishMessage = PublishMessage.buildPayload(
            topicName = "user/log",
            payload = "yolo".toBuffer(),
            qos = QualityOfService.EXACTLY_ONCE,
            packetIdentifier = 13
        )
        val buffer = PlatformBuffer.allocate(18)
        publishMessage.serialize(buffer)
        publishMessage.payload?.position(0)
        buffer.resetForRead()
        val firstByte = buffer.readUnsignedByte()
        assertEquals(firstByte.toInt().shr(4), 3, "fixed header control packet type")
        assertFalse(firstByte.get(3), "fixed header publish dup flag")
        assertTrue(firstByte.get(2), "fixed header qos bit 2")
        assertFalse(firstByte.get(1), "fixed header qos bit 1")
        assertFalse(firstByte.get(0), "fixed header retain flag")
        assertEquals(buffer.readVariableByteInteger(), 16, "fixed header remaining length")
        assertEquals(8u, buffer.readUnsignedShort(), "variable header topic name length")
        assertEquals("user/log", buffer.readUtf8(8u).toString(), "variable header topic name value")
        if (publishMessage.variable.packetIdentifier != null) {
            assertEquals(buffer.readUnsignedShort().toInt(), publishMessage.variable.packetIdentifier)
        }
        assertEquals("yolo", buffer.readUtf8(4u).toString(), "payload value")
        buffer.resetForRead()
        val byte1 = buffer.readUnsignedByte()
        val remainingLength = buffer.readVariableByteInteger()
        val result = PublishMessage.from(buffer, byte1, remainingLength)
        assertEquals(publishMessage, result)
    }

    @Test
    fun genericSerializationPublishRetainFlag() {
        val publishMessage =
            PublishMessage.buildPayload(topicName = "user/log", payload = "yolo".toBuffer(), retain = true)
        val buffer = PlatformBuffer.allocate(16)
        publishMessage.serialize(buffer)
        publishMessage.payload?.position(0)
        buffer.resetForRead()
        val firstByte = buffer.readUnsignedByte()
        assertEquals(firstByte.toInt().shr(4), 3, "fixed header control packet type")
        assertFalse(firstByte.get(3), "fixed header publish dup flag")
        assertFalse(firstByte.get(2), "fixed header qos bit 2")
        assertFalse(firstByte.get(1), "fixed header qos bit 1")
        assertTrue(firstByte.get(0), "fixed header retain flag")
        assertEquals(buffer.readVariableByteInteger(), 14, "fixed header remaining length")
        assertEquals(8u, buffer.readUnsignedShort(), "variable header topic name length")
        assertEquals("user/log", buffer.readUtf8(8u).toString(), "variable header topic name value")
        if (publishMessage.variable.packetIdentifier != null) {
            assertEquals(buffer.readUnsignedShort().toInt(), publishMessage.variable.packetIdentifier)
        }
        assertEquals("yolo", buffer.readUtf8(4u).toString(), "payload value")
        buffer.resetForRead()
        val byte1 = buffer.readUnsignedByte()
        val remainingLength = buffer.readVariableByteInteger()
        val result = PublishMessage.from(buffer, byte1, remainingLength)
        assertEquals(publishMessage, result)
    }

    @Test
    fun nullGenericSerialization() {
        val publishMessage = PublishMessage.build(topicName = "user/log")
        val buffer = PlatformBuffer.allocate(12)
        publishMessage.serialize(buffer)
        buffer.resetForRead()
        val firstByte = buffer.readUnsignedByte()
        assertEquals(firstByte.toInt().shr(4), 3, "fixed header control packet type")
        assertFalse(firstByte.get(3), "fixed header publish dup flag")
        assertFalse(firstByte.get(2), "fixed header qos bit 2")
        assertFalse(firstByte.get(1), "fixed header qos bit 1")
        assertFalse(firstByte.get(0), "fixed header retain flag")
        assertEquals(buffer.readVariableByteInteger(), 10, "fixed header remaining length")
        assertEquals(8u, buffer.readUnsignedShort(), "variable header topic name length")
        assertEquals("user/log", buffer.readUtf8(8u).toString(), "variable header topic name value")
        if (publishMessage.variable.packetIdentifier != null) {
            assertEquals(buffer.readUnsignedShort().toInt(), publishMessage.variable.packetIdentifier)
        }
        buffer.resetForRead()
        val byte1 = buffer.readUnsignedByte()
        val remainingLength = buffer.readVariableByteInteger()
        val result = PublishMessage.from(buffer, byte1, remainingLength)
        publishMessage.payload?.resetForRead()
        assertEquals(publishMessage, result)
    }
}
