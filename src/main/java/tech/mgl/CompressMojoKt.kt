package tech.mgl

import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.yahoo.platform.yui.compressor.CssCompressor
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.StringUtils
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.codehaus.plexus.util.FileUtils
import tech.mgl.closure.compiler.CompressJs
import java.io.*
import java.net.URLEncoder
import java.nio.file.Files
import java.util.regex.Pattern

/**
 * @author hotpot
 * @date 2021.07
 */
@Slf4j
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
class CompressMojoKt: CompressJs() {
    private var htmlCompressor: HtmlCompressor? = null
    private val listIncludes: ArrayList<String> = ArrayList(0)


    @Throws(java.lang.Exception::class)
    private fun processIncludes() {
        for (filePath in listIncludes) {
            try {
                val file = File(filePath)
                val newPath = StringBuilder(0)
                newPath.append(file.parent).append("/")
                newPath.append(FileUtils.basename(file.name))
                newPath.append("min.")
                newPath.append(FileUtils.extension(file.name))

                //getLog().info(filePath.concat("................"));
                val writeFile = File("$newPath.t")
                when (FileUtils.extension(file.name).toLowerCase()) {
                    "js" -> {

                        /*Reader reader = new FileReader(file);
                    JavaScriptCompressor jsCompressor = new JavaScriptCompressor(reader, new MGLErrorReport());*/

                        /*Writer writer = new FileWriter(writeFile);
                    jsCompressor.compress(writer, lineBreak, false, false, false, false);
                    reader.close();
                    writer.flush();
                    writer.close();*/
                        val comCnt = compressJS(Files.readString(file.toPath()))
                        Files.writeString(writeFile.toPath(), comCnt)
                        if (overWrite) {
                            file.delete()
                            Files.copy(writeFile.toPath(), File(filePath).toPath())
                            writeFile.delete()
                        }
                    }
                    "css" -> {
                        val reader: Reader = FileReader(file)
                        val cssCompressor = CssCompressor(reader)
                        //File writeFile = new File(newPath.toString().concat(".t"));
                        val writer: Writer = FileWriter(writeFile)
                        cssCompressor.compress(writer, lineBreak)
                        reader.close()
                        writer.flush()
                        writer.close()
                        if (overWrite) {
                            file.delete()
                            Files.copy(writeFile.toPath(), File(filePath).toPath())
                            writeFile.delete()
                        }
                    }
                    "html" -> {
                        try {
                            val content = Files.readString(file.toPath())
                            if (StringUtils.isBlank(content)) {
                                return
                            }
                            val compressContent = htmlCompressor!!.compress(content)
                            if (overWrite) {
                                if (!file.canWrite()) {
                                    file.setWritable(true)
                                }
                                Files.writeString(file.toPath(), compressJSForInHTML(compressContent))
                            } else {
                                val compressFile = File(newPath.toString())
                                Files.writeString(compressFile.toPath(), compressJSForInHTML(compressContent))
                            }
                        } catch (eh: java.lang.Exception) {
                            getLog().error(eh.message, eh)
                            runWhenOnErrorHtml(file, newPath.toString())
                        }
                    }
                }
                //getLog().info("Compress File ".concat(file.getName()).concat(" Successfully !"));
            } catch (e: java.lang.Exception) {
                getLog().error(e.message, e)
                //遇错跳出压缩
                if (!continueWhenError) {
                    throw java.lang.Exception(e)
                }
            }
        }
    }

    private fun runWhenOnErrorHtml(file: File, newPath: String) {
        try {
            /*var rep = /\n+/g;
                        var repone = /<!--.*?-->/ig;
                        var reptwo = /\/\*.*?\*\//ig;
                        var reptree = /[ ]+</ig;
                        var sourceZero = source.replace(rep,"");
                        var sourceOne = sourceZero.replace(repone,"");
                        var sourceTwo = sourceOne.replace(reptwo,"");
                        var sourceTree = sourceTwo.replace(reptree,"<");
                        $("#result").val(sourceTree);
                        var resultLength = sourceTwo.length;
                        var range = 100-(resultLength/sourceLength*100);
                        $("#old").text(sourceLength);
                        $("#new").text(resultLength);
                        $("#range").text(range.toFixed(2));*/
            val rep = "\n"
            val repone = "\\<!--.*?-->"
            val reptwo = "\\*.*?\\*"
            val reptree = "\\[ ]+<"
            val content = Files.readString(file.toPath())
            if (StringUtils.isBlank(content)) {
                return
            }
            val compressContent = content.replace(rep.toRegex(), "")
                .replace(repone.toRegex(), "")
                .replace(reptwo.toRegex(), "")
                .replace(reptree.toRegex(), "<")
            /*
                    String compressContent = htmlCompressor.compress(content);*/if (overWrite) {
                if (!file.canWrite()) {
                    file.setWritable(true)
                }
                Files.writeString(file.toPath(), compressContent)
            } else {
                val compressFile = File(newPath)
                Files.writeString(compressFile.toPath(), compressContent)
            }

            //getLog().info("Normal Compress(support es6) File ".concat(file.getName()).concat(" Successfully !"));
        } catch (e: java.lang.Exception) {
            getLog().error(e.message, e)
        }
    }

    /**
     * 对含有中文的字符串进行Unicode编码
     * \ue400 \u9fa5 Unicode表中的汉字的头和尾
     */
    @Throws(java.lang.Exception::class)
    fun setUrlForChn(url: String?): String? {
        val regEx = "[\u4e00-\u9fa5]"
        val p = Pattern.compile(regEx)
        val m = p.matcher(url)
        val sb = StringBuffer()
        while (m.find()) {
            m.appendReplacement(sb, URLEncoder.encode(m.group(), "UTF-8"))
        }
        m.appendTail(sb)
        return sb.toString()
    }

    /**
     * 单独压缩HTML内部JS内容 避免压缩ES6语法错误问题
     *
     * @param htmlStr
     * @return
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun compressJSForInHTML(htmlStr: String?): String? {
        // System.out.println(htmlStr);
        //String c = htmlStr;
        val regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>" //定义script的正则表达式
        val regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>" //定义style的正则表达式
        val regEx_html = "<[^>]+>" //定义HTML标签的正则表达式
        val p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE)
        val m_script = p_script.matcher(htmlStr)
        //System.out.println(m_script.group(1).toString());
        var i = 0
        val sb = StringBuilder(0)
        val scriptStartTag = "<script[^>]*?>"
        val scriptEndTag = "<\\/script>"
        while (m_script.find()) {
            val group = m_script.group()
            val matchContent = group.replace(scriptStartTag.toRegex(), "").replace(scriptEndTag.toRegex(), "")
            if (StringUtils.isBlank(matchContent)) {
                continue
            }


            // System.out.println(m_script.start());
            // System.out.println(m_script.end());
            val com = super.compressJS(matchContent)
            val s = getMatch(group, scriptStartTag) + com.replace("\\$".toRegex(), "RDS_CHAR_DOLLAR") + getMatch(
                group,
                scriptEndTag
            )
            //System.out.println(setUrlForChn(s));
            //c = c.substring(0, m_script.start()).concat(com).concat(htmlStr.substring(m_script.end()));

            //替换内容的反斜杠用特殊字符代替 替换完整之后在改回来 否则替换文字的自动把反斜杠删除 不知道为什么
            m_script.appendReplacement(
                sb,
                getMatch(group, scriptStartTag) + com.replace("\\$".toRegex(), "RDS_CHAR_DOLLAR")
                    .replace("\\\\".toRegex(), "BACK_SLASH") + getMatch(group, scriptEndTag)
            )
            //System.out.println(i);
            //System.out.println(sb.toString());
            //System.out.println("1:" + super.compressJS(matchContent));
            i++
        }
        //System.out.println(sb.toString());
        m_script.appendTail(sb)
        //System.out.println(sb);
/*
        htmlStr=m_script.replaceAll(""); //过滤

        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
        Matcher m_style=p_style.matcher(htmlStr);
        htmlStr=m_style.replaceAll(""); //过滤style标签

        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html=p_html.matcher(htmlStr);
        htmlStr=m_html.replaceAll(""); //过滤html标签*/


        //return "";
        return sb.toString().replace("RDS_CHAR_DOLLAR".toRegex(), "\\$")
            .replace("BACK_SLASH".toRegex(), "\\\\") //返回文本字符串
    }

    private fun getMatch(str: String, pattern: String): String {
        val r = Pattern.compile(pattern)
        // 现在创建 matcher 对象
        val m = r.matcher(str)
        if (m.find()) {
            //getLog().info("Found value: " + m.group() );
            /*System.out.println("Found value: " + m.group(1) );
            System.out.println("Found value: " + m.group(2) );
            System.out.println("Found value: " + m.group(3) );*/
            return m.group()
        } else {
            //getLog().error("NO MATCH");
            //System.out.println("NO MATCH");
        }
        return ""
    }

    @Throws(IOException::class)
    private fun intoDir(file: File) {
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                //getLog().info("DEBUG:".concat(f.getName()));
                intoDir(f)
            }
        }
        try {
            if (!file.isFile) {
                return
            }

            //取消只对static目录有效 以便支持 Html后缀的 模板引擎
            /*if (!file.getAbsolutePath().contains("/static/") && !file.getAbsolutePath().contains("\\static\\")) {
                return;
            }*/if (StringUtils.isBlank(file.name) || file.name.length == 0) return
            /**
             * 只支持 js css html文件的压缩, 并且跳过已压缩 .min.命名的文件
             */
            if (!"js".equals(FileUtils.extension(file.name), ignoreCase = true)
                && !"css".equals(FileUtils.extension(file.name), ignoreCase = true)
                && !"html".equals(FileUtils.extension(file.name), ignoreCase = true)
            ) {
                return
            }
            if (file.name.equals(".min.", ignoreCase = true)) {
                return
            }
            var pass = false
            if (null != includes && includes.size > 0) {
                for (include in includes) {
                    pass = checkStr(file.absolutePath, include)
                    if (pass) {
                        break
                    }
                }
            }
            if (pass && null != excludes && excludes.size > 0) {
                for (exclude in excludes) {
                    pass = !checkStr(file.absolutePath, exclude)
                    //System.out.println(pass);
                    if (!pass) {
                        break
                    }
                }
            }
            if (pass) {
                listIncludes.add(file.absolutePath)
            }
        } catch (e: Exception) {
            getLog().error("Error File : " + file.absolutePath)
            log.error(e.message, e)
        }
    }
    override fun process() {
        getLog().info("Running mgl-compress ... ")
        val f = outputDirectory
        if (!f.exists()) {
            f.mkdirs()
        }

        try {
            //init
            if (null == htmlCompressor) {
                //System.out.println("---------------------");
                htmlCompressor = HtmlCompressor()
            }

            htmlCompressor!!.isEnabled = true
            htmlCompressor!!.isCompressCss = true
            htmlCompressor!!.isYuiJsPreserveAllSemiColons = true
            htmlCompressor!!.yuiJsLineBreak = 1
            htmlCompressor!!.isPreserveLineBreaks = false
            htmlCompressor!!.isRemoveIntertagSpaces = true
            htmlCompressor!!.isRemoveComments = true
            htmlCompressor!!.isRemoveMultiSpaces = true
            /*htmlCompressor = new HtmlCompressor();
            htmlCompressor.setCompressCss(true);
            htmlCompressor.setCompressJavaScript(true);
            htmlCompressor.setRemoveComments(true);
            htmlCompressor.setRemoveIntertagSpaces(true);
            htmlCompressor.setRemoveMultiSpaces(true);
            htmlCompressor.setRemoveComments(true);*/

            intoDir(outputDirectory)
            processIncludes()
        } catch (e: IOException) {
            throw MojoExecutionException("Error ", e)
        }
    }
}