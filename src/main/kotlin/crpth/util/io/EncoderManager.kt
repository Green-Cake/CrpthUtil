package crpth.util.io

import crpth.util.vec.writeString
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object EncoderManager {

    private val map = mutableMapOf<KClass<*>, IEncoder>()

    init {

        register(Encoder(String::class) { value, stream ->
            check(value::class.isSubclassOf(this.type)) {
                "The type of specified parameter is illegal!"
            }
            stream.writeString(value as String)

        })

    }

    fun register(encoder: IEncoder) {

        map[encoder.type] = encoder

    }

    operator fun get(clazz: KClass<*>) = map[clazz]

    operator fun contains(clazz: KClass<*>) = map.contains(clazz)

}