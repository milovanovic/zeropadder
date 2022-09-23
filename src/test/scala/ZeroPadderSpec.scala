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

  it should f"work for UInt" in {
    val params: ZeroPadderParams[UInt] = ZeroPadderParams(
      proto = UInt(8.W),
      packetSizeStart = 5,
      packetSizeEnd = 8,
      numberOfPackets = 3,
      queueDepth = 8,
      useBlockRam = false
    )
    Random.setSeed(11110L)
    val in = Seq.fill(params.numberOfPackets)(Seq.fill(params.packetSizeStart)(Random.nextInt(1<<(params.proto.getWidth)).toDouble)) // max is exclusive 0 is inclusive
    dsptools.Driver.execute(
      () => new ZeroPadderNative(params),
      Array(
      "--backend-name", "verilator", // "treadle", // "verilator",
      "--target-dir", s"test_run_dir/zeropadder")
      ) { c =>
        new ZeroPadderTester(c, in, 0)
      } should be (true)
  }
}
