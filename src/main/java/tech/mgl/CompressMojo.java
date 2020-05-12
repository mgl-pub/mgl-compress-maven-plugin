package tech.mgl;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * @author mgl.tech
 * @date 2020-05
 */
@Mojo(name = "compress",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompressMojo
        extends BaseMojo {

    private HtmlCompressor htmlCompressor;

    private void intoDir(File file) throws IOException {
        if (null == htmlCompressor) {
            htmlCompressor = new HtmlCompressor();
            htmlCompressor.setCompressCss(true);
            htmlCompressor.setCompressJavaScript(true);
            htmlCompressor.setRemoveComments(true);
            htmlCompressor.setRemoveIntertagSpaces(true);
            htmlCompressor.setRemoveMultiSpaces(true);
            htmlCompressor.setRemoveComments(true);
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                intoDir(f);
            }
        }

        try {
            if (file.isFile()) {
                if (!file.getAbsolutePath().contains("/static/") && !file.getAbsolutePath().contains("\\static\\")) {
                    return;
                }

                if (file.getName().endsWith(".js") || file.getName().endsWith(".css") || file.getName().endsWith(".html")) {
                    String content = Files.readString(file.toPath());
                    if (StringUtils.isBlank(content)) {
                        return;
                    }

                    if (StringUtils.isBlank(file.getName()) || file.getName().length() == 0)
                        return;

                    String compressContent = htmlCompressor.compress(content);
                    if (overWrite) {
                        if (!file.canWrite()) {
                            file.setWritable(true);
                        }
                        Files.writeString(file.toPath(), compressContent);
                        log.info("Compress File ", file.getName(), " Successfully !");
                        getLog().info("Compress File ".concat(file.getName()).concat(" Successfully !"));
                        return;
                    }

                    StringBuffer newPath = new StringBuffer(0);
                    newPath.append(file.getParent()).append("/");

                    newPath.append(FileUtils.basename(file.getName()));
                    newPath.append("min.");
                    newPath.append(FileUtils.extension(file.getName()));
                    File compressFile = new File(newPath.toString());
                    Files.writeString(compressFile.toPath(), compressContent);
                    log.info("Compress File ", file.getName(), " Successfully !");
                    getLog().info("Compress File ".concat(file.getName()).concat(" Successfully !"));
                }
            }
        } catch (Exception e) {
            getLog().error("Error File : ".concat(file.getAbsolutePath()));
            log.error("Error File : " , file.getAbsolutePath());
            log.error(e.getMessage(),e);
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
            intoDir(outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Error " , e);
        }
    }
}
