package crpth.util.io

import java.io.DataOutputStream

fun DataOutputStream.encodeWith(vararg props: Any) {
    props.forEach {
        when  {
            it is IEncodable -> it.encode(this)
            it::class in EncoderManager -> EncoderManager[it::class]!!.encode(it, this)
            else -> throw Error("The type ${it::class} cannot be encoded!")
        }
    }
}