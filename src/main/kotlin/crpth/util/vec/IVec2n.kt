package crpth.util.vec

import crpth.util.io.IEncodable

/**
 * Presents two numeric value [x] and [y].
 *
 * As some classes that implements this interface are value class, or inlined,
 * do not specify this interface directly for a type of any parameter or argument lest the performance should lower.
 *
 * This interface is mainly used for regulation of implementation.
 *
 * @see Vec2b
 * @see Vec2s
 * @see Vec2i
 * @see Vec2f
 * @see Vec2d
 */
internal interface IVec2n<T, Self : IVec2n<T, Self>> : IEncodable where T : Number, T : Comparable<T> {

    val x: T
    val y: T

    fun toVec2b() = Vec2b(x.toByte(), y.toByte())

    fun toVec2s() = Vec2s(x.toShort(), y.toShort())

    fun toVec2i() = Vec2i(x.toInt(), y.toInt())

    fun toVec2f() = Vec2f(x.toFloat(), y.toFloat())

    fun toVec2d() = Vec2d(x.toDouble(), y.toDouble())

    fun coerceIn(xRange: ClosedRange<T>, yRange: ClosedRange<T> = xRange): Self

    fun copy(x: Number = this.x, y: Number = this.y): Self

    fun dropX() = copy(x = 0)

    fun dropY() = copy(y = 0)

}