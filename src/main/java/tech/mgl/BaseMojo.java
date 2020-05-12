package tech.mgl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * @author mgl.tech
 * @date 2020-05
 */
abstract class BaseMojo
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
    @Parameter(defaultValue = "${project.build.directory}")
    protected File outputDirectory;

    @Parameter(name = "overWrite", defaultValue = "false")
    protected boolean overWrite = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void process() throws Exception;

}
