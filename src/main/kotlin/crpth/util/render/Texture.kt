package crpth.util.render

import crpth.util.ResourceAccessor
import crpth.util.vec.Vec2i
import org.lwjgl.opengl.GL11.*
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class Texture(val id: Int) {

    companion object {

        internal fun load(domain: String, path: String): Texture {

            val (pixels, size) = ResourceAccessor.loadTextureImageBufAndSize(ClassLoader.getSystemResourceAsStream("assets/$domain/textures/$path") ?: throw NoSuchFileException(File(
                "assets/$domain/textures/$path"
            )))
            return load(pixels, size)

        }

        internal fun load(pixels: ByteBuffer, size: Vec2i): Texture {
            val id = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, id)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glBindTexture(GL_TEXTURE_2D, 0)
            return Texture(id)
        }

        internal fun loadGrayscale(pixels: ByteBuffer, size: Vec2i): Texture {
            val id = glGenTextures()
            glPixelStorei(GL_UNPACK_ALIGNMENT,1)
            glBindTexture(GL_TEXTURE_2D, id)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, size.x, size.y, 0, GL_ALPHA, GL_UNSIGNED_BYTE, pixels)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glBindTexture(GL_TEXTURE_2D, 0)
            return Texture(id)
        }

        internal fun load(img: BufferedImage): Texture {

            val (buffer, _) = ResourceAccessor.loadTextureImageBufAndSize(img)

            val id = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, id)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.width, img.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glBindTexture(GL_TEXTURE_2D, 0)

            return Texture(id)
        }

        internal fun createLazyInit(domain: String, path: String): Lazy<Texture> = lazy {
            load(domain, path)
        }

    }

    /**
     * Be careful! this function works only when the texture has been bound; otherwise never call this.
     */
    fun getWidth(): Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH)

    /**
     * Be careful! this function works only when the texture has been bound; otherwise never call this.
     */
    fun getHeight(): Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)

    /**
     * Be careful! this function works only when the texture has been bound; otherwise never call this.
     */
    fun getAspectRatio() = getWidth().toFloat() / getHeight().toFloat()

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun debind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun delete() {
        glDeleteTextures(id)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <R> use(block: Texture.()->R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return try {
            bind()
            block(this)
        } finally {
            debind()
        }
    }

}

