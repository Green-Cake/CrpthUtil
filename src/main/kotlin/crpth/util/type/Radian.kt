package crpth.util.type

import kotlin.math.PI

/**
 * @property value bigger than -PI and less than PI.
 */
@JvmInline
value class Radian private constructor(val value: Double) {

    companion object {

        fun of(radian: Double) = Radian(radian % PI)

        fun of(degree: Degree) = of(degree.value / 180 * PI)

    }

}