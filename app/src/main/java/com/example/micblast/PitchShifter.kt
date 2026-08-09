package com.example.micblast

import kotlin.math.abs

/**
 * Simple real-time granular pitch shifter.
 *
 * Keeps a rolling circular buffer of recent audio and reads it back
 * with two overlapping, crossfaded "grains" whose read speed differs
 * from the write speed. Reading faster than real-time raises pitch
 * (chipmunk); reading slower lowers it (deep voice). Output length
 * always matches input length, so it drops straight into a live
 * mic -> speaker loop without messing up timing.
 */
class PitchShifter(sampleRateHz: Int) {

    private val grainSize = (sampleRateHz * 0.05).toInt().coerceAtLeast(256) // ~50ms grains
    private val bufSize = grainSize * 4
    private val circBuf = FloatArray(bufSize)

    private var writePtr = 0
    private var phase1 = 0
    private var phase2 = grainSize / 2
    private var readPos1 = 0f
    private var readPos2 = 0f
    private var initialized = false

    fun process(input: ShortArray, len: Int, pitchFactor: Float, output: ShortArray) {
        if (!initialized) {
            readPos1 = (writePtr - grainSize).toFloat()
            readPos2 = (writePtr - grainSize / 2).toFloat()
            initialized = true
        }

        for (i in 0 until len) {
            circBuf[writePtr] = input[i].toFloat()

            val env1 = triangleWindow(phase1)
            val s1 = interpolate(readPos1)
            val env2 = triangleWindow(phase2)
            val s2 = interpolate(readPos2)

            var outSample = s1 * env1 + s2 * env2
            if (outSample > 32767f) outSample = 32767f
            if (outSample < -32768f) outSample = -32768f
            output[i] = outSample.toInt().toShort()

            readPos1 += pitchFactor
            readPos2 += pitchFactor
            phase1++
            phase2++
            if (phase1 >= grainSize) {
                phase1 = 0
                readPos1 = (writePtr - grainSize).toFloat()
            }
            if (phase2 >= grainSize) {
                phase2 = 0
                readPos2 = (writePtr - grainSize).toFloat()
            }
            // Wrap writePtr manually instead of letting it grow forever —
            // at 44.1kHz an unbounded Int would overflow (and go negative)
            // after ~13.5 hours of continuous use in a pitch-shifted mode,
            // which would then index circBuf with a negative value and crash.
            writePtr++
            if (writePtr >= bufSize) writePtr -= bufSize
        }
    }

    private fun triangleWindow(phase: Int): Float {
        val t = phase.toFloat() / grainSize
        return 1f - abs(2f * t - 1f)
    }

    private fun interpolate(pos: Float): Float {
        var p = pos % bufSize
        if (p < 0) p += bufSize
        val i0 = p.toInt()
        val i1 = (i0 + 1) % bufSize
        val frac = p - i0
        return circBuf[i0] * (1 - frac) + circBuf[i1] * frac
    }
}
