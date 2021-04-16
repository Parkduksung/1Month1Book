package com.example.prototypepattern

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ProtoTypePatternActivityTest {
    private val standardOut: PrintStream? = System.out
    private val outputStreamCaptor: ByteArrayOutputStream = ByteArrayOutputStream()

    @Before
    fun setUp() {
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @After
    fun tearDown() {
        System.setOut(standardOut)
    }


    @Test
    fun `clone 을 하여 생성시, 올바르게 출력된다`() {

        //given
        val protoTypePatternA = ProtoTypePattern()

        //when
        val protoTypePatternB = protoTypePatternA.clone()

        //then
        assertEquals("cloneProtoTypePattern", outputStreamCaptor.toString())
    }
}