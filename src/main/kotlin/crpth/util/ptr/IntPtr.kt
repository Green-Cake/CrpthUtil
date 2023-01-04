package crpth.util.ptr

import org.lwjgl.BufferUtils
import java.nio.IntBuffer

@JvmInline
value class IntPtr private constructor(val ptr: IntBuffer = BufferUtils.createIntBuffer(1)) {

    companion object {
        fun alloc() = IntPtr()
    }

    val value get() = ptr[0]

}