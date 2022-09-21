package zeropadder

import chisel3._
import dsptools.numbers._

case class ZeroPadderParams[T <: Data: Real](
  proto: T,
  packetSizeStart: Int,
  packetSizeEnd:   Int,
  queueDepth:      Int,
  numberOfPackets:   Int,
  useBlockRam:     Boolean
) {

  def checkNumberOfSamples {
    require(packetSizeStart < packetSizeEnd)
  }
  def checkQueueDepth {
    require(queueDepth > (packetSizeEnd - packetSizeStart))
  }
}

// TODO: Maybe add last signal genearation after every packet if that is defined inside specific register!
