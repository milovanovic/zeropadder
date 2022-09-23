package zeropadder

import chisel3._
import dsptools.numbers._

case class ZeroPadderParams[T <: Data: Real](
  proto: T,
  packetSizeStart: Int,
  packetSizeEnd:   Int,
  queueDepth:      Int,    // make this as an Option parameter
  numberOfPackets: Int,
  useQueue:        Boolean,
  useBlockRam:     Boolean // make this as an Option parameter
) {

  def checkNumberOfSamples {
    require(packetSizeStart <= packetSizeEnd)
  }
  def checkQueueDepth {
    require(queueDepth >= packetSizeEnd)
  }
}
// TODO: Generation of last signal after each packet - to make it configurable both in compile and run-time
