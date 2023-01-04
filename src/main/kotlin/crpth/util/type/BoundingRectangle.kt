package crpth.util.type

import crpth.util.vec.Vec2f

interface IBoundingRectangle {

    operator fun contains(p: Vec2f): Boolean

//    operator fun contains(p: IVec2n<*, *>) = contains(p.toVec2f())

    fun intersects(other: BoundingRectangle): Boolean

}

open class BoundingRectangle(val pos: Vec2f, val size: Vec2f) : IBoundingRectangle {

    override operator fun contains(p: Vec2f) = pos.x <= p.x && pos.y <= p.y && p.x <= pos.x + size.x && p.y <= pos.y + size.y

    override fun intersects(other: BoundingRectangle): Boolean {

        val c0 = pos + size / 2.0f
        val c1 = other.pos + size / 2.0f
        val d = (c0 - c1).abs
        val s = (this.size + size) / 2.0f

        return d.x < s.x && d.y < s.y

    }

}