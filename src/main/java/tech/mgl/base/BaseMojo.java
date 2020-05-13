package tech.mgl.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

/**
 * @author mgl.tech
 * @date 2020-05
 */
public abstract class BaseMojo
        extends AbstractMojo {
    protected Logger log = LogManager.getLogger(this.getClass());

    @Parameter(defaultValue = "${project.basedir}")
    protected File baseDir;

    @Parameter(defaultValue = "${project.build.sourceDirectory}")
    protected File sourceDirectory;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}")
    protected File testSourceDirectory;

    @Parameter
    protected String[] includes;

    @Parameter
    protected String[] excludes;

    @Parameter(defaultValue = "UTF-8")
    private String encoding;

    @Parameter(defaultValue = "false")
    private boolean preserveAllSemiColons;

    @Parameter(defaultValue = "false")
    private boolean disableOptimizations;

    @Parameter(defaultValue = "false")
    private String obfuscate; //混淆 默认不混淆

    @Parameter(defaultValue = ".min")
    private String suffix;

    @Parameter(defaultValue = "${project.build.directory}")
    protected File outputDirectory;

    @Parameter(name = "overWrite", defaultValue = "false")
    protected boolean overWrite = false;

    @Parameter(name = "lineBreak", defaultValue = "1500")
    protected int lineBreak;

    protected String[] getDefaultIncludes() throws Exception {
        return new String[]{"**/*.css", "**/*.js"};
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (includes != null) {
                getLog().info("includes:");
                for (String include : includes) {
                    getLog().info(include);
                }
            }

            if (excludes != null) {
                getLog().info("excludes:");
                for (String exclude : excludes) {
                    getLog().info(exclude);
                }
            }

            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void process() throws Exception;

    /**
     *
     * @param str
     * @param pattern
     * @return
     */
    protected boolean checkStr(String str,String pattern) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher( "glob:".concat(pattern));
        boolean matches = matcher.matches(Paths.get(str));
        return matches;
    }

    public static void main(String[] args) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher( "glob:".concat("**/res/pub/js/cjs/*.js"));
        boolean matches = matcher.matches(Paths.get("/res\\pub/js/cjs/sdfsdf.js"));

        System.out.println(matches);
    }
}
