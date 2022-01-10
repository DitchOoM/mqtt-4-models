@file:OptIn(ExperimentalMultiplatform::class)

package com.ditchoom.mqtt3.controlpacket

@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class Parcelize()