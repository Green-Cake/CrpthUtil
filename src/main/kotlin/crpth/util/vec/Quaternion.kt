package crpth.util.vec

import kotlin.math.sqrt

data class Quaternion(val w: Double, val i: Double=0.0, val j: Double=0.0, val k: Double=0.0) {

    companion object {

        val UNIT_W = Quaternion(1.0, 0.0, 0.0, 0.0)
        val UNIT_I = Quaternion(0.0, 1.0, 0.0, 0.0)
        val UNIT_J = Quaternion(0.0, 0.0, 1.0, 0.0)
        val UNIT_K = Quaternion(0.0, 0.0, 0.0, 1.0)

        fun fromVector(vec: Vec3d) = Quaternion(0.0, vec.x, vec.y, vec.z)

    }

    val vector get() = Vec3d(i, j, k)

    val conj get() = Quaternion(w, -i, -j, -k)

    val norm get() = sqrt(w*w + i*i + j*j + k*k)

    val reciprocal get() = conj / (w*w + i*i + j*j + k*k)

    operator fun plus(other: Quaternion) = Quaternion(w + other.w, i + other.i, j + other.j, k + other.k)

    operator fun minus(other: Quaternion) = Quaternion(w - other.w, i - other.i, j - other.j, k - other.k)

    operator fun times(other: Quaternion) = Quaternion(
        w*other.w - i*other.i - j*other.j - k*other.k,
        w*other.i + i*other.w + j*other.k - k*other.j,
        w*other.j - i*other.k + j*other.w + k*other.i,
        w*other.k + i*other.j - j*other.i + k*other.w
    )

    operator fun times(other: Double) = Quaternion(w*other, i*other, j*other, k*other)

    operator fun unaryMinus() = Quaternion(-w, -i, -j, -k)

    operator fun div(other: Double) = Quaternion(w/other, i/other, j/other, k/other)

}