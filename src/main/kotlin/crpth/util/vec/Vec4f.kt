package crpth.util.vec

import java.awt.Color
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

data class Vec4f(val a: Float, val b: Float, val c: Float, val d: Float=1.0f) {

    companion object {

        val ZERO = Vec4f(0f, 0f, 0f, 0f)
        val ONE = Vec4f(1f, 1f, 1f, 1f)

        val BLACK = Vec4f(0f, 0f, 0f, 1f)
        val WHITE = Vec4f(1f, 1f, 1f, 1f)

        fun grayscale(f: Float) = Vec4f(f, f, f)

        fun from(awtColor: Color) = Vec4f(awtColor.red / 255f, awtColor.green / 255f, awtColor.blue / 255f, awtColor.alpha / 255f)

    }

    fun withAlpha(d: Float) = copy(d = d)

    operator fun plus(other: Vec4f) = Vec4f(a + other.a, b + other.b, c + other.c, d + other.d)

    operator fun minus(other: Vec4f) = Vec4f(a - other.a, b - other.b, c - other.c, d - other.d)

    operator fun times(other: Vec4f) = Vec4f(a * other.a, b * other.b, c * other.c, d * other.d)

    operator fun div(other: Vec4f) = Vec4f(a / other.a, b / other.b, c / other.c, d / other.d)

    operator fun times(other: Float) = Vec4f(a * other, b * other, c * other, d * other)

    operator fun div(other: Float) = Vec4f(a / other, b / other, c / other, d / other)

    @OptIn(ExperimentalContracts::class)
    inline fun computeEach(block: (Float)->Float): Vec4f {

        contract {
            callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
        }

        return Vec4f(block(a), block(b), block(c), block(d))
    }

}