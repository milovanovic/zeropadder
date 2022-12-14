package zeropadder

import chisel3._
import chisel3.experimental.FixedPoint

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
//import org.scalatest.{FlatSpec, Matchers}
import scala.util.{Random}

//class ZeroPaddersSpec extends FlatSpec with Matchers {
class ZeroPadderSpec extends AnyFlatSpec with Matchers {

  behavior of "Zero padding module"

  // useBlockRam needs to be always on true -> todo: make it work as well when useBlockRam is not true
  // queue depth is set to be 2*packetSizeEnd + packetSizeEnd/2
  //for (numberOfPackets <- Seq(3, 8, 5)) {
  for (numberOfPackets <- Seq(5, 8)) {
    for (packetSizeEnd <- Seq(16, 32)) {
      val packetSizesStart = (2 to packetSizeEnd by 4).toSeq
      for (packetSizeStart <- packetSizesStart) {
        for (useQueue <- Seq(true, false)) {
          for (useBlockRam <- Seq(true)) {
        //for (useBlockRam <- Seq(true)) {
          it should f"work for data type UInt(8.W), ct parameters numberOfPackets = $numberOfPackets, packetSizeEnd = $packetSizeEnd, useBlockRam = $useBlockRam, useQueue = $useQueue , rt parameter packetSizeStart = $packetSizeStart" in {
            val params: ZeroPadderParams[UInt] = ZeroPadderParams(
              proto = UInt(8.W),
              packetSizeStart = packetSizeEnd,
              packetSizeEnd = packetSizeEnd,
              numberOfPackets = numberOfPackets,
              queueDepth = packetSizeEnd*2 + packetSizeEnd/2,
              useQueue = useQueue,
              useBlockRam = true
            )
            Random.setSeed(11110L)
            val in = Seq.fill(params.numberOfPackets)(Seq.fill(packetSizeStart)(Random.nextInt(1<<(params.proto.getWidth)).toDouble)) // max is exclusive 0 is inclusive
            dsptools.Driver.execute(
              () => new ZeroPadderNative(params),
              Array(
              "--backend-name", "verilator", // "treadle", // "verilator",
              "--target-dir", s"test_run_dir/zeropadder")
              ) { c =>
                new ZeroPadderTester(c, in, 0, packetSizeStart)
              } should be (true)
            }
          }
        }
      }
    }
  }
}
