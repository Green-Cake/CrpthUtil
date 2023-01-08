package crpth.util.io

import java.io.DataOutputStream

fun DataOutputStream.encodeWith(vararg props: Any) {
    props.forEach {
        if (it is IEncodable)
            it.encode(this)
        else
            throw Error("The type ${it::class} cannot be encoded!")
    }
}