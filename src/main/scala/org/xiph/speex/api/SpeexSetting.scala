package org.xiph.speex.api

import javax.sound.sampled.AudioFormat
import org.xiph.speex.{ SpeexDecoder => XiphDecoder }
import org.xiph.speex.{ SpeexEncoder => XiphEncoder }

sealed abstract class Channels(val channelCount: Int)
case object MonoChannel extends Channels(1)
case object StereoChannel extends Channels(2)

case class BandChannel(band: Band, channels: Channels)

sealed abstract class Band(val bytes: Int, val samplingRate: Int, val frameRate: Int) {
  def Mono = BandChannel(this, MonoChannel)
  def Stereo = BandChannel(this, StereoChannel)
}
case object NarrowBand extends Band(320, 8000, 8000)
case object WideBand extends Band(640, 16000, 16000)
case object UltraWideBand extends Band(1280, 32000, 32000)

object SpeexSetting extends SpeexSetting(0, 6, NarrowBand.Mono)

case class SpeexSetting(mode: Int, quality: Int, bandChannel: BandChannel) {
  val sampleRate = bandChannel.band.samplingRate
  val frameRate = bandChannel.band.frameRate
  val bandModeBytes = bandChannel.band.bytes
  val channels = bandChannel.channels.channelCount

  def targetDataLineAudioFormat: AudioFormat = {
    val bytesPerFrame = 2; val sampleSizeInBits = 16;
    val signed = true; val bigEndian = false;
    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      sampleRate, sampleSizeInBits, channels, bytesPerFrame, frameRate, bigEndian);
  }

  private def speexAudioFormat = new AudioFormat(org.xiph.speex.spi.SpeexEncoding.SPEEX,
    sampleRate,
    -1, // sample size in bits
    channels,
    -1, // frame size
    -1, // frame rate
    false)

  def playableAudioFormat = new AudioFormat(sampleRate,
    16, // sample size in bits
    channels,
    true, // signed (PCM signed or unsigned)
    false); // little endian (for PCM wav)

  /**
   * the minimum frame size to be encoded, larger sizes must be multiples of this.
   */
  def encodableFrameSize = bandModeBytes * channels

  /**
   * create new empty buffer at the minimum encodable frame size
   */
  def newEncodableBuffer = new Array[Byte](encodableFrameSize)

  def newEncoder = {
    val speexEncoder = new XiphEncoder()
    speexEncoder.init(mode, quality, sampleRate, channels);

    //speexEncoder.getEncoder().setComplexity(3);
    //speexEncoder.getEncoder().setBitRate(bitrate);
    //speexEncoder.getEncoder().setVbr(useVariableBitRate);
    //speexEncoder.getEncoder().setVbrQuality(8f);
    //speexEncoder.getEncoder().setVad(vad);
    //speexEncoder.getEncoder().setDtx(dtx);

    SpeexEncoder(speexEncoder)
  }

  def newDecoder = {
    val speexDecoder = new XiphDecoder()
    speexDecoder.init(mode, sampleRate, channels, true);

    SpeexDecoder(speexDecoder)
  }

}
case class SpeexEncoder(underlying: XiphEncoder) {
  def encode(source: Array[Byte]) = apply(source: Array[Byte])
  def apply(source: Array[Byte]): Array[Byte] = {
    underlying.processData(source.asInstanceOf[Array[Byte]], 0, source.length);

    val encodedData = new Array[Byte](underlying.getProcessedDataByteSize);
    underlying.getProcessedData(encodedData, 0);
    encodedData
  }
}

case class SpeexDecoder(underlying: XiphDecoder) {
  def decode(source: Array[Byte]) = apply(source: Array[Byte])
  def apply(source: Array[Byte]): Array[Byte] = {
    underlying.processData(source.asInstanceOf[Array[Byte]], 0, source.length);

    val decoded = new Array[Byte](underlying.getProcessedDataByteSize);
    underlying.getProcessedData(decoded, 0);

    decoded
  }
}
