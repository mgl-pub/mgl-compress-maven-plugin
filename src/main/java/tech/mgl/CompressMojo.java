package tech.mgl;

import com.google.javascript.jscomp.CompilerOptions;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.mozilla.javascript.ErrorReporter;
import tech.mgl.closure.compiler.CompressJs;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
            File file = new File(filePath);

            StringBuffer newPath = new StringBuffer(0);
            newPath.append(file.getParent()).append("/");

            newPath.append(FileUtils.basename(file.getName()));
            newPath.append("min.");
            newPath.append(FileUtils.extension(file.getName()));

            getLog().info(filePath.concat("................"));
            switch (FileUtils.extension(file.getName()).toLowerCase()) {
                case "js": {
                    /*Reader reader = new FileReader(file);
                    JavaScriptCompressor jsCompressor = new JavaScriptCompressor(reader, new MGLErrorReport());*/
                    File writeFile = new File(newPath.toString().concat(".t"));
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
                    File writeFile = new File(newPath.toString().concat(".t"));
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
                    String content = Files.readString(file.toPath());
                    if (StringUtils.isBlank(content)) {
                        return;
                    }
                    String compressContent = htmlCompressor.compress(content);
                    if (overWrite) {
                        if (!file.canWrite()) {
                            file.setWritable(true);
                        }
                        Files.writeString(file.toPath(), compressContent);
                    } else {
                        File compressFile = new File(newPath.toString());
                        Files.writeString(compressFile.toPath(), compressContent);
                    }
                    break;
                }
            }
            getLog().info("Compress File ".concat(file.getName()).concat(" Successfully !"));
        }
    }

    private void intoDir(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                intoDir(f);
            }
        }

        try {
            if (null == file || !file.isFile()) {
                return;
            }

            if (!file.getAbsolutePath().contains("/static/") && !file.getAbsolutePath().contains("\\static\\")) {
                return;
            }

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

            if (null != excludes && excludes.length > 0) {
                for (String exclude : excludes) {
                    pass = !checkStr(file.getAbsolutePath(), exclude);
                    if (pass) {
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
        getLog().info("run mgl-compress ... ");
        File f = outputDirectory;
        if (!f.exists()) {
            f.mkdirs();
        }

        try {
            //init
            if (null == htmlCompressor) {
                htmlCompressor = new HtmlCompressor();
                htmlCompressor.setCompressCss(true);
                htmlCompressor.setCompressJavaScript(true);
                htmlCompressor.setRemoveComments(true);
                htmlCompressor.setRemoveIntertagSpaces(true);
                htmlCompressor.setRemoveMultiSpaces(true);
                htmlCompressor.setRemoveComments(true);
            }
            intoDir(outputDirectory);
            processIncludes();
        } catch (IOException e) {
            throw new MojoExecutionException("Error ", e);
        }
    }
}
