package zeropadder

import chisel3._
import chisel3.util._
import dsptools._
import dsptools.numbers._
import dsputils._ // because of SyncReadMem
import chisel3.experimental.FixedPoint
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.internal.requireIsChiselType

// For native design, two ZeroPadderNative needs to be added to support DspComplex data type
// Q: How to implement that type can be DspComplex as well that will be very useful - it is not important which type of data it is
class ZeroPadderNative [T <: Data: Real] (val params: ZeroPadderParams[T]) extends Module {

  params.checkNumberOfSamples
  params.checkQueueDepth

  val io = IO(ZeroPadderIO(params))

  val packetSizeStartReg = RegNext(io.packetSizeStart, 0.U)
  val packetSizeEndReg = RegNext(io.packetSizeEnd, 0.U)
  val numberOfPackets = RegNext(io.numberOfPackets, 0.U)

  val log2packetSizeStart = log2Ceil(params.packetSizeStart)
  val log2packetSizeEnd = log2Ceil(params.packetSizeEnd)
  val log2numberOfPackets = log2Ceil(params.numberOfPackets)

  val numZeros = packetSizeStartReg - packetSizeEndReg
  val cntOutData = RegInit(0.U(log2packetSizeStart.W))
  val cntPackets = RegInit(0.U(log2numberOfPackets.W))
  val zeroPaddFlag = Wire(Bool())
  val dataQueue = Module(new QueueWithSyncReadMem(params.proto.cloneType, entries = params.queueDepth, flow = true, useSyncReadMem = params.useBlockRam, useBlockRam = params.useBlockRam))
  val outFire = io.out.valid && io.out.ready

  when (outFire) {
    cntOutData := cntOutData + 1.U
  }

  when (cntOutData === (packetSizeEndReg - 1.U) && outFire) { //  check out.fire
    cntOutData := 0.U
    when (cntPackets === (numberOfPackets - 1.U)) {
      io.lastOut := true.B //&& genLast // active only one signal of clock
      cntPackets := 0.U
    }
    .otherwise {
      cntPackets := cntPackets + 1.U
      io.lastOut := false.B //&& genLast
    }
  }
  .otherwise {
    io.lastOut := false.B //&& genLast
  }

  when (cntOutData < packetSizeStartReg) { //< or equal to
    zeroPaddFlag := false.B
  }
  .otherwise {
    zeroPaddFlag := true.B
  }

  val rstProto = Wire(io.in.bits.cloneType)
  rstProto := Real[T].fromDouble(0.0)

  val inValidReg = RegNext(io.in.valid, init = false.B)
  val inDataReg = RegNext(io.in.bits, init = rstProto)

  dataQueue.io.enq.bits  := inDataReg
  dataQueue.io.enq.valid := inValidReg
  dataQueue.io.deq.ready := ~zeroPaddFlag && io.out.ready

  when (zeroPaddFlag) { // if out_ready is not active then cntOutData will not count at all
    io.out.valid := true.B
    io.out.bits := rstProto
    //in.ready := out.ready // can accept new data but that data is stored inside queue
    // but in theory this block should be always ready to accept data
    io.in.ready := dataQueue.io.enq.ready
  }
  .otherwise {
    io.out.valid := dataQueue.io.deq.valid
    io.out.bits := dataQueue.io.deq.bits
    io.in.ready := dataQueue.io.enq.ready
  }
}

object ZeroPadderApp extends App
{
  val params: ZeroPadderParams[FixedPoint] = ZeroPadderParams(
    proto = FixedPoint(16.W, 14.BP),
    packetSizeStart = 16,
    packetSizeEnd  = 32,
    queueDepth = 64,
    numberOfPackets = 3,
    useBlockRam = false
  )
  // switch to new chisel stage!
  chisel3.Driver.execute(args, () => new ZeroPadderNative(params))
}
