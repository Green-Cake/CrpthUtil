package crpth.util.render

import crpth.util.ResourceAccessor
import crpth.util.vec.Vec2i
import org.lwjgl.opengl.GL11.*
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer

@JvmInline
value class Texture(override val id: Int) : ITexture {

    companion object {

        @Deprecated("Use ResourceManager instead!", ReplaceWith("ResourceManager.STATIC.loadTexture(path)"))
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

        @Deprecated("Use ResourceManager instead!", ReplaceWith("ResourceManager.STATIC.loadTextureLazy(path)"))
        internal fun createLazyInit(domain: String, path: String): Lazy<Texture> = lazy {
            load(domain, path)
        }

    }

    override fun <R> use(block: ITexture.()->R): R = try {
        bind()
        block(this)
    } finally {
        debind()
    }

}

