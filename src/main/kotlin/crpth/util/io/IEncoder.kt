package crpth.util.io

import java.io.DataOutputStream
import kotlin.reflect.KClass

interface IEncoder {

    val type: KClass<*>

    fun encode(value: Any, stream: DataOutputStream)

}

class Encoder(override val type: KClass<*>, val fEncode: IEncoder.(value: Any, stream: DataOutputStream)->Unit) : IEncoder {
    override fun encode(value: Any, stream: DataOutputStream) = fEncode(value, stream)
}