package tech.mgl.base

import org.apache.logging.log4j.LogManager
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

abstract class NewBaseMojo: AbstractMojo() {

    protected var log = LogManager.getLogger(this.javaClass)

    @Parameter(defaultValue = "\${project.basedir}")
    protected var baseDir: File? = null

    @Parameter(defaultValue = "\${project.build.sourceDirectory}")
    protected var sourceDirectory: File? = null

    @Parameter(defaultValue = "\${project.build.testSourceDirectory}")
    protected var testSourceDirectory: File? = null

    @Parameter
    protected lateinit var includes: Array<String>

    @Parameter
    protected lateinit var excludes: Array<String>

    @Parameter(defaultValue = "UTF-8")
    private val encoding: String? = null

    @Parameter(defaultValue = "false")
    private val preserveAllSemiColons = false

    @Parameter(defaultValue = "false")
    private val disableOptimizations = false

    @Parameter(defaultValue = "false")
    private val obfuscate //混淆 默认不混淆
            : String? = null

    @Parameter(defaultValue = ".min")
    private val suffix: String? = null

    @Parameter(defaultValue = "\${project.build.directory}")
    protected var outputDirectory: File? = null

    @Parameter(name = "overWrite", defaultValue = "false")
    protected var overWrite = false

    @Parameter(name = "lineBreak", defaultValue = "1500")
    protected var lineBreak = 0

    @Parameter(name = "continueWhenError", defaultValue = "true")
    protected var continueWhenError = true

    @Throws(Exception::class)
    protected open fun getDefaultIncludes(): Array<String>? {
        return arrayOf("**/*.css", "**/*.js")
    }

    /**
     * 处理设定参数
     */
    override fun execute() {
        try {
            getLog().info("includes:")
            for (include in includes) {
                getLog().info(include)
            }
            getLog().info("excludes:")
            for (exclude in excludes) {
                getLog().info(exclude)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}