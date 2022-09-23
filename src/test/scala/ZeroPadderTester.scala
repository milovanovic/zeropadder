package zeropadder

import chisel3._
import dsptools.DspTester
import dsptools.numbers._
import scala.util.Random

class ZeroPadderTester[T <: Data](dut: ZeroPadderNative[T], in: Seq[Seq[Double]], tol: Int) extends DspTester(dut) {
  // here some kind of requirements should be added, check inp size and parameters of dut
  //require(dut.params.numberOfPackets == in.length)
  //require(in.foreach())
  //Random.setSeed(11110L)

  val inputData = in.flatten // this is total number of data -> if number of data is more than expected then
  val numZeros = dut.params.packetSizeEnd - dut.params.packetSizeStart
  var expectedData = in.map{c => c ++: Seq.fill(numZeros)(0.0)}.flatten

  expectedData.foreach { c => println(c.toString) }
  val input1 = inputData.iterator

  var inValid = 0
  updatableDspVerbose.withValue(true) { // maybe true will become false

    poke(dut.io.packetSizeStart, dut.params.packetSizeStart)
    poke(dut.io.packetSizeEnd, dut.params.packetSizeEnd)
    poke(dut.io.numberOfPackets, dut.params.numberOfPackets)
    step(5)

    while(!expectedData.isEmpty) {
      inValid = Random.nextInt(2)
      poke(dut.io.out.ready, Random.nextInt(2))
        if (input1.hasNext) {
          poke(dut.io.in.valid, inValid)
          if (peek(dut.io.in.ready) == true && peek(dut.io.in.valid) == true) {
            poke(dut.io.in.bits, input1.next())
          }
        }
      if (peek(dut.io.out.ready) == true && peek(dut.io.out.valid) == true) {
        dut.params.proto match {
          case dspR: DspReal => realTolDecPts.withValue(tol) { expect(dut.io.out.bits, expectedData.head) }
          case _ =>  fixTolLSBs.withValue(tol) { expect(dut.io.out.bits, expectedData.head) }
        }
        val lastOut = peek(dut.io.lastOut)
        println(lastOut.toString)
        if (expectedData.length == 1) {
          expect(dut.io.lastOut, 1)
        }
        expectedData = expectedData.tail
      }
      // check last signal as well!
      step(1)
    }
  }
  poke(dut.io.in.valid, 0)
  step(50)
}
