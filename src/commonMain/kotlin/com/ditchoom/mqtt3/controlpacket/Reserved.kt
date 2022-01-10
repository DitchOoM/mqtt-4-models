package com.ditchoom.mqtt3.controlpacket

import com.ditchoom.mqtt3.controlpacket.Parcelize
import com.ditchoom.mqtt.controlpacket.IReserved
import com.ditchoom.mqtt.controlpacket.format.fixed.DirectionOfFlow

@Parcelize
object Reserved : ControlPacketV4(0, DirectionOfFlow.FORBIDDEN), IReserved
