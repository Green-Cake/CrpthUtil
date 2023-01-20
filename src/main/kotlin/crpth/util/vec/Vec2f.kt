package crpth.util.vec

import java.io.DataOutputStream
import kotlin.math.absoluteValue

@JvmInline
value class Vec2f(val data: ULong) : IVec2n<Float, Vec2f> {

    companion object {

        val ZERO = Vec2f(0.0f, 0.0f)
        val ONE = Vec2f(1.0f, 1.0f)

    }

    constructor(x: Float, y: Float) : this((x.toRawBits().resizeToULong() shl 32) + y.toRawBits().resizeToULong())

    override fun coerceIn(xRange: ClosedRange<Float>, yRange: ClosedRange<Float>) = Vec2f(x.coerceIn(xRange), y.coerceIn(yRange))

    val abs get() = Vec2f(x.absoluteValue, y.absoluteValue)
    override val x get() = Float.fromBits((data shr 32).toInt())

    override val y get() = Float.fromBits(data.toInt())

    override fun copy(x: Number, y: Number) = Vec2f(x.toFloat(), y.toFloat())

    override fun encode(stream: DataOutputStream) {
        stream.writeLong(data.toLong())
    }

    operator fun plus(other: Vec2f) = Vec2f(x + other.x, y + other.y)

    operator fun minus(other: Vec2f) = Vec2f(x - other.x, y - other.y)

    operator fun times(other: Vec2f) = Vec2f(x * other.x, y * other.y)

    operator fun div(other: Vec2f) = Vec2f(x / other.x, y / other.y)

    fun plus(x: Float=0f, y: Float=0f) = Vec2f(this.x + x, this.y + y)

    fun minus(x: Float=0f, y: Float=0f) = Vec2f(this.x - x, this.y - y)

    fun times(x: Float=1f, y: Float=1f) = Vec2f(this.x * x, this.y * y)

    fun div(x: Float=1f, y: Float=1f) = Vec2f(this.x / x, this.y / y)

    operator fun times(other: Float) = Vec2f(x * other, y * other)

    operator fun div(other: Float) = Vec2f(x / other, y / other)

    override fun dropX() = Vec2f(data and 0x00000000_FFFFFFFFFu)

    override fun dropY() = Vec2f(data and 0xFFFFFFFF_00000000u)

    fun invertX() = Vec2f(-x, y)

    fun invertY() = Vec2f(x, -y)

    infix fun dot(other: Vec2f) = x*other.x + y*other.y

    override fun toString() = "($x, $y)"

    operator fun component1() = x

    operator fun component2() = y

}