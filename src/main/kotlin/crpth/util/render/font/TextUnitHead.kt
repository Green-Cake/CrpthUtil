package crpth.util.render.font

import crpth.util.vec.Vec2f
import java.util.*

sealed interface TextUnit {

    companion object {

        fun new(str: String, position: Vec2f, height: Float, config: FontConfig): TextUnit {

            val lines = str.lines()

            var chained: TextUnit = TextUnitHead(lines[0], position, height, config)

            lines.drop(1).forEachIndexed { index, s ->
                chained = chained.br().chain(s, height, config)
            }

            return chained

        }

    }

    val str: String

    val height: Float

    val config: FontConfig

    fun getStack(): Stack<TextUnit>

    fun chainFromSuper(str: String, height: Float=this.height, config: FontConfig=this.config): TextUnit

    fun chain(str: String, height: Float=this.height, config: FontConfig=this.config): TextUnit {

        var chained: TextUnit = this

        val lines = str.lines()

        lines.forEachIndexed { index, s ->
            chained = Chained(chained, s, height, config)
            if(index != lines.lastIndex)
                chained = chained.br()
        }

        return chained as Chained

    }

    fun br(): TextUnit = Br(this, height, config)

}

internal class TextUnitHead internal constructor(override val str: String, val position: Vec2f, override val height: Float, override val config: FontConfig) : TextUnit {

    override fun getStack() = Stack<TextUnit>().apply { push(this@TextUnitHead) }

    override fun chainFromSuper(str: String, height: Float, config: FontConfig) = TextUnitHead(str, position, height, config)

}

internal open class Chained(val before: TextUnit, override val str: String, override val height: Float, override val config: FontConfig) : TextUnit {

    override fun getStack(): Stack<TextUnit> {

        val stack = Stack<TextUnit>()

        stack.push(this)

        do {
            stack.push((stack.peek() as Chained).before)
        } while(stack.peek() !is TextUnitHead)

        return stack

    }

    override fun chainFromSuper(str: String, height: Float, config: FontConfig) = before.chain(str, height, config)

}

internal class Br(before: TextUnit, height: Float, config: FontConfig) : Chained(before, "", height, config) {

    override val str: String get() = ""

    override fun getStack(): Stack<TextUnit> {

        val stack = Stack<TextUnit>()

        stack.push(this)

        do {
            stack.push((stack.peek() as Chained).before)
        } while(stack.peek() !is TextUnitHead)

        return stack

    }

}
