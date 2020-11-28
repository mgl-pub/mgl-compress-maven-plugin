package tech.mgl;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.yahoo.platform.yui.compressor.CssCompressor;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import tech.mgl.closure.compiler.CompressJs;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mgl.tech
 * @date 2020-05
 */
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompressMojo
        extends CompressJs {
    private HtmlCompressor htmlCompressor;
    private final List<String> listIncludes = new ArrayList<>(0);

    private void processIncludes() throws Exception {
        for (String filePath : listIncludes) {
            try {
                File file = new File(filePath);
                StringBuilder newPath = new StringBuilder(0);
                newPath.append(file.getParent()).append("/");

                newPath.append(FileUtils.basename(file.getName()));
                newPath.append("min.");
                newPath.append(FileUtils.extension(file.getName()));

                //getLog().info(filePath.concat("................"));
                File writeFile = new File(newPath.toString().concat(".t"));
                switch (FileUtils.extension(file.getName()).toLowerCase()) {
                    case "js": {
                    /*Reader reader = new FileReader(file);
                    JavaScriptCompressor jsCompressor = new JavaScriptCompressor(reader, new MGLErrorReport());*/

                    /*Writer writer = new FileWriter(writeFile);
                    jsCompressor.compress(writer, lineBreak, false, false, false, false);
                    reader.close();
                    writer.flush();
                    writer.close();*/
                        String comCnt = compressJS(Files.readString(file.toPath()));
                        Files.writeString(writeFile.toPath(), comCnt);
                        if (overWrite) {
                            file.delete();
                            Files.copy(writeFile.toPath(), new File(filePath).toPath());
                            writeFile.delete();
                        }
                        break;
                    }
                    case "css": {
                        Reader reader = new FileReader(file);
                        CssCompressor cssCompressor = new CssCompressor(reader);
                        //File writeFile = new File(newPath.toString().concat(".t"));
                        Writer writer = new FileWriter(writeFile);
                        cssCompressor.compress(writer, lineBreak);
                        reader.close();
                        writer.flush();
                        writer.close();

                        if (overWrite) {
                            file.delete();
                            Files.copy(writeFile.toPath(), new File(filePath).toPath());
                            writeFile.delete();
                        }
                        break;
                    }
                    case "html": {
                        try {
                            String content = Files.readString(file.toPath());
                            if (StringUtils.isBlank(content)) {
                                return;
                            }
                            String compressContent = htmlCompressor.compress(content);
                            if (overWrite) {
                                if (!file.canWrite()) {
                                    file.setWritable(true);
                                }
                                Files.writeString(file.toPath(), compressJSForInHTML(compressContent));
                            } else {
                                File compressFile = new File(newPath.toString());
                                Files.writeString(compressFile.toPath(), compressJSForInHTML(compressContent));
                            }
                        } catch (Exception eh) {
                            getLog().error(eh.getMessage(), eh);
                            runWhenOnErrorHtml(file, newPath.toString());
                        }
                        break;
                    }
                }
                //getLog().info("Compress File ".concat(file.getName()).concat(" Successfully !"));
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
                //遇错跳出压缩
                if (!continueWhenError) {
                    throw new Exception(e);
                }
            }
        }
    }

    private void runWhenOnErrorHtml(File file, String newPath) {
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

            String rep = "\n";
            String repone = "\\<!--.*?-->";
            String reptwo = "\\*.*?\\*";
            String reptree = "\\[ ]+<";

            String content = Files.readString(file.toPath());
            if (StringUtils.isBlank(content)) {
                return;
            }

            String compressContent = content.replaceAll(rep, "")
                    .replaceAll(repone, "")
                    .replaceAll(reptwo, "")
                    .replaceAll(reptree, "<");
/*
                    String compressContent = htmlCompressor.compress(content);*/
            if (overWrite) {
                if (!file.canWrite()) {
                    file.setWritable(true);
                }
                Files.writeString(file.toPath(), compressContent);
            } else {
                File compressFile = new File(newPath);
                Files.writeString(compressFile.toPath(), compressContent);
            }

            //getLog().info("Normal Compress(support es6) File ".concat(file.getName()).concat(" Successfully !"));
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    /**
     * 对含有中文的字符串进行Unicode编码
     * \ue400 \u9fa5 Unicode表中的汉字的头和尾
     */
    public static String setUrlForChn(String url) throws Exception{
        String regEx = "[\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(url);
        StringBuffer sb = new StringBuffer();
        while(m.find()){
            m.appendReplacement(sb, URLEncoder.encode(m.group(), "UTF-8"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 单独压缩HTML内部JS内容 避免压缩ES6语法错误问题
     *
     * @param htmlStr
     * @return
     * @throws Exception
     */
    public String compressJSForInHTML(String htmlStr) throws Exception {
       // System.out.println(htmlStr);
        //String c = htmlStr;
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        //System.out.println(m_script.group(1).toString());
        int i = 0;
        StringBuilder sb = new StringBuilder(0);
        String scriptStartTag = "<script[^>]*?>";
        String scriptEndTag = "<\\/script>";
        while (m_script.find()) {
            String group = m_script.group();
            String matchContent = group.replaceAll(scriptStartTag, "").replaceAll(scriptEndTag, "");
            if (StringUtils.isBlank(matchContent)) {
                continue;
            }


           // System.out.println(m_script.start());
           // System.out.println(m_script.end());
            String com = super.compressJS(matchContent);
            String s = (getMatch(group,scriptStartTag).concat(com.replaceAll("\\$", "RDS_CHAR_DOLLAR")).concat(getMatch(group,scriptEndTag)));
            //System.out.println(setUrlForChn(s));
            //c = c.substring(0, m_script.start()).concat(com).concat(htmlStr.substring(m_script.end()));

            //替换内容的反斜杠用特殊字符代替 替换完整之后在改回来 否则替换文字的自动把反斜杠删除 不知道为什么
            m_script.appendReplacement(sb, getMatch(group,scriptStartTag).concat(com.replaceAll("\\$", "RDS_CHAR_DOLLAR").replaceAll("\\\\","BACK_SLASH")).concat(getMatch(group,scriptEndTag)));
            //System.out.println(i);
            //System.out.println(sb.toString());
            //System.out.println("1:" + super.compressJS(matchContent));
            i++;
        }
        //System.out.println(sb.toString());
        m_script.appendTail(sb);
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
       return sb.toString().replaceAll("RDS_CHAR_DOLLAR","\\$").replaceAll("BACK_SLASH","\\\\"); //返回文本字符串
    }

    private static String getMatch(String str,String pattern) {
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(str);
        if (m.find( )) {
            //getLog().info("Found value: " + m.group() );
            /*System.out.println("Found value: " + m.group(1) );
            System.out.println("Found value: " + m.group(2) );
            System.out.println("Found value: " + m.group(3) );*/
            return m.group();
        } else {
            //getLog().error("NO MATCH");
            //System.out.println("NO MATCH");
        }
        return "";
    }

    public static void main(String[] args) throws Exception {
        /*HtmlCompressor compressor = new HtmlCompressor();
        compressor.setEnabled(true);
        compressor.setCompressCss(true);
        compressor.setYuiJsPreserveAllSemiColons(true);
        compressor.setYuiJsLineBreak(1);
        compressor.setPreserveLineBreaks(false);
        compressor.setRemoveIntertagSpaces(true);
        compressor.setRemoveComments(true);
        compressor.setRemoveMultiSpaces(true);*/

        String rep = "\n";
        /*String repone = "\\<!--.*?-->";*/
       /* String reptwo = "\\*.*?\\*";
        String reptree = "\\[ ]+<";*/

        String content = Files.readString(Path.of("F:\\Dev\\SomeWorkspace\\inspection-new\\inspection-old\\target\\classes\\static\\efbaseinfo\\efInfoList.html"));
        if (StringUtils.isBlank(content)) {
            return;
        }

        //System.out.println(new CompressMojo().compressJSForInHTML(content));

        //System.out.println(compressor.compress(content));
        String compressContent = content.replaceAll(rep, "")
               /*  .replaceAll(repone, "")
               .replaceAll(reptwo, "")
                .replaceAll(reptree, "<")*/;

        //System.out.println(compressContent);






        String regEx_script = "\\{[\\s\\S]*?}"; //定义script的正则表达式
        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher("油中{元素[elements_h]，}{元素[elements]}；");
        StringBuilder sb = new StringBuilder(0);
        String scriptStartTag = "\\{";
        String scriptEndTag = "}";
        while (m_script.find()) {
            String group = m_script.group();
            String matchContent = group.replaceAll(scriptStartTag, "").replaceAll(scriptEndTag, "");
            if (StringUtils.isBlank(matchContent)) {
                continue;
            }
            String s = (getMatch(group,scriptStartTag).concat("替换的内容").concat(getMatch(group,scriptEndTag))).replaceAll("[{}]","");
            System.out.println(s);
            m_script.appendReplacement(sb,s);
        }
        m_script.appendTail(sb);
        System.out.println(sb);

    }

    private void intoDir(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                //getLog().info("DEBUG:".concat(f.getName()));
                intoDir(f);
            }
        }

        try {
            if (!file.isFile()) {
                return;
            }

            //取消只对static目录有效 以便支持 Html后缀的 模板引擎
            /*if (!file.getAbsolutePath().contains("/static/") && !file.getAbsolutePath().contains("\\static\\")) {
                return;
            }*/

            if (StringUtils.isBlank(file.getName()) || file.getName().length() == 0)
                return;

            /**
             * 只支持 js css html文件的压缩, 并且跳过已压缩 .min.命名的文件
             */
            if (!"js".equalsIgnoreCase(FileUtils.extension(file.getName()))
                    && !"css".equalsIgnoreCase(FileUtils.extension(file.getName()))
                    && !"html".equalsIgnoreCase(FileUtils.extension(file.getName()))) {

                return;
            }

            if (file.getName().equalsIgnoreCase(".min.")) {
                return;
            }

            boolean pass = false;
            if (null != includes && includes.length > 0) {
                for (String include : includes) {
                    pass = checkStr(file.getAbsolutePath(), include);
                    if (pass) {
                        break;
                    }
                }
            }

            if (pass && null != excludes && excludes.length > 0) {
                for (String exclude : excludes) {
                    pass = !checkStr(file.getAbsolutePath(), exclude);
                    //System.out.println(pass);
                    if (!pass) {
                        break;
                    }
                }
            }

            if (pass) {
                listIncludes.add(file.getAbsolutePath());
            }
        } catch (Exception e) {
            getLog().error("Error File : ".concat(file.getAbsolutePath()));
            log.error(e.getMessage(), e);
        }

    }

    @Override
    protected void process() throws Exception {
        getLog().info("Running mgl-compress ... ");
        File f = outputDirectory;
        if (!f.exists()) {
            f.mkdirs();
        }

        try {
            //init
            if (null == htmlCompressor) {
                //System.out.println("---------------------");
                htmlCompressor = new HtmlCompressor();
                htmlCompressor.setEnabled(true);
                htmlCompressor.setCompressCss(true);
                htmlCompressor.setYuiJsPreserveAllSemiColons(true);
                htmlCompressor.setYuiJsLineBreak(1);
                htmlCompressor.setPreserveLineBreaks(false);
                htmlCompressor.setRemoveIntertagSpaces(true);
                htmlCompressor.setRemoveComments(true);
                htmlCompressor.setRemoveMultiSpaces(true);
                /*htmlCompressor = new HtmlCompressor();
                htmlCompressor.setCompressCss(true);
                htmlCompressor.setCompressJavaScript(true);
                htmlCompressor.setRemoveComments(true);
                htmlCompressor.setRemoveIntertagSpaces(true);
                htmlCompressor.setRemoveMultiSpaces(true);
                htmlCompressor.setRemoveComments(true);*/
            }
            intoDir(outputDirectory);
            processIncludes();
        } catch (IOException e) {
            throw new MojoExecutionException("Error ", e);
        }
    }
}
