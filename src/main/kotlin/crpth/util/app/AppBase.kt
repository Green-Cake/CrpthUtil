package crpth.util.app

import crpth.util.ResourceManager
import crpth.util.RichWindow
import crpth.util.Window
import crpth.util.controller.ControllerManager
import crpth.util.logging.Logger
import crpth.util.mouse.MouseAction
import crpth.util.mouse.MouseButton
import crpth.util.render.Renderer
import crpth.util.vec.Vec2i
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11

abstract class AppBase(val domain: String, val title: String, val windowSize: Vec2i = Vec2i(1280, 960)) {

    open val logger: Logger get() = Logger.Muted

    var window = Window.NULL_W
        private set

    val richWindow: RichWindow by lazy {
        RichWindow(window, this::onClicked)
    }

    val renderer = Renderer { window }

    val resourceManager = ResourceManager(domain)

    val controller by lazy { ControllerManager(richWindow) }

    fun run() {

        init()

        loop()

    }

    open fun onClicked(button: MouseButton, action: MouseAction): Boolean = false

    private fun init() {

        logger.info("LWJGL Version: ${Version.getVersion()}")

        GLFWErrorCallback.createPrint(System.err).set()

        if(!GLFW.glfwInit()) {
            throw IllegalStateException("Failed to initialize GLFW...")
        }

//        val monitor = GLFW.glfwGetPrimaryMonitor()
//        val mode = GLFW.glfwGetVideoMode(monitor)

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, 1)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, 1)

        window = Window.create(windowSize.x, windowSize.y, title)
        if(window.isNull()) {
            throw RuntimeException("Failed to create GLFW window...")
        }

        richWindow.init()

        window.makeContextCurrent()
        GLFW.glfwSwapInterval(1)

        GL.createCapabilities()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glCullFace(GL11.GL_BACK)

        onInit()

        window.show()

    }

    private fun loop() = try {

        GL.createCapabilities()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glCullFace(GL11.GL_BACK)

        while(!window.shouldClose()) { //loop start

            update()
            render()

            window.swapBuffers()
            GLFW.glfwPollEvents()
        }

    } finally {}

    protected open fun update() {

        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glLoadIdentity()

        onUpdate()

        controller.update()
        richWindow.update()
    }

    protected open fun render() {

        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        onRender(renderer)

    }

    abstract fun onInit()

    abstract fun onUpdate()

    abstract fun onRender(renderer: Renderer)

}