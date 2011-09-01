package org.sonatype.nexus.bundle.launcher.jsw;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Commandline;
import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.bundle.launcher.internal.AntHelper;
import org.sonatype.nexus.bundle.launcher.util.RequestUtils;

/**
 * Helper to perform operations on JSW bundle scripts.
 */
public class JSWExecSupport {

    private Logger logger = LoggerFactory.getLogger(JSWExecSupport.class);

    private final File binDir;
    private final String appName;
    private final File controlScript;
    private final String controlScriptCanonicalPath;
    private final AntHelper ant;

    /**
     *
     * @param binDir the bin directory where the jsw control scripts are located
     * @param appName the app name managed by JSW
     * @throws NullPointerException if params are null
     * @throws IllegalArgumentException if the JSW exec script cannot be found for this platform
     */
    public JSWExecSupport(final File binDir, final String appName, final AntHelper ant) {
        Preconditions.checkNotNull(binDir);
        Preconditions.checkNotNull(appName);
        Preconditions.checkNotNull(ant);

        this.ant = ant;

        if(!binDir.isDirectory()){
            throw new IllegalArgumentException("binDir is not a directory:" + binDir.getAbsolutePath());
        }

        this.binDir = binDir;

        if(appName.trim().equals("")){
            throw new IllegalArgumentException("appName must contain at least one character");
        }

        this.appName = appName;

        boolean windows = Os.isFamily(Os.FAMILY_WINDOWS);
        final String extension = windows ? ".bat" : "";
        this.controlScript = new File(binDir, appName + extension);

        if(!this.controlScript.isFile() || !this.controlScript.canExecute()){
            throw new IllegalArgumentException("jsw script is not an executable file: " + this.controlScript.getAbsolutePath());
        }

        try {
            this.controlScriptCanonicalPath = this.controlScript.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem getting canonical path to jsw control script: " + this.controlScript.getAbsolutePath(), e);
        }

    }

    protected File getControlScript(){
        return this.controlScript;
    }

    /**
     *
     * @return true if started, false if could not detect Nexus as started
     */
    public boolean startAndWaitUntilReady(final String nexusBaseURL) {
        //need console since on windows we would first need a service installed if start cmd was used
        executeJSWScript("console");
        return RequestUtils.waitForNexusToStart(nexusBaseURL);
    }

    /**
     * Stop the server using cmd line script.
     * <p>
     * This method is more reliable when you need the server completely stopped
     * before continuing.
     */
    public void stop() {
        executeJSWScript("stop", false);
    }

    protected void executeJSWScript(final String command) {
        executeJSWScript(command, true);
    }



    protected void executeJSWScript(final String command, final boolean spawn) {
        File script = getControlScript();
        ExecTask exec = ant.createTask(ExecTask.class);
        exec.setExecutable(this.controlScriptCanonicalPath);

        if (spawn) {
            exec.setSpawn(true);
        } else {
            exec.setFailIfExecutionFails(true);
            exec.setFailonerror(true);
        }
        Commandline.Argument arg = exec.createArg();
        arg.setValue(command);
        exec.setDir(script.getParentFile());
        logger.info("Executing {} script cmd {} {}", new Object[]{appName, this.controlScriptCanonicalPath, command});
        exec.execute();
    }


}
