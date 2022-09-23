package zeropadder

import chisel3._
import chisel3.util._
import dsptools.numbers._

class ZeroPadderIO[T <: Data: Real] (params: ZeroPadderParams[T]) extends Bundle {
  val in = Flipped(Decoupled(params.proto))
  val lastIn = Input(Bool())
  val out = Decoupled(params.proto)
  val lastOut = Output(Bool())

  val packetSizeStart = Input(UInt((log2Ceil(params.packetSizeStart+1).W)))
  val packetSizeEnd = Input(UInt((log2Ceil(params.packetSizeEnd+1).W)))
  val numberOfPackets = Input(UInt(log2Ceil(params.numberOfPackets + 1).W)) // check if +1 is necessary to have here

  override def cloneType: this.type = ZeroPadderIO(params).asInstanceOf[this.type]
}

object ZeroPadderIO {
  def apply[T <: Data : Real](params: ZeroPadderParams[T]): ZeroPadderIO[T] = new ZeroPadderIO(params)
}
