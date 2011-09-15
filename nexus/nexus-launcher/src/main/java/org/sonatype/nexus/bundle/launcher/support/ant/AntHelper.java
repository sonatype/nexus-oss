/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher.support.ant;

import com.google.common.base.Preconditions;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.PrintStream;

/**
 * Wrapper for invoking Ant tasks.
 *
 * @since 1.9.3
 */
public class AntHelper {

    private final Project ant;

    @Inject
    private Logger logger = LoggerFactory.getLogger(AntHelper.class);

    @Inject
    public AntHelper(@Named("${basedir}") final File basedir) {
        Preconditions.checkNotNull(basedir);

        ant = new Project();
        initAntLogger(logger, ant);
        ant.setBaseDir(basedir);
        ant.init();
    }

    protected void initAntLogger(final Logger logger, final Project ant) {
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(ant);

        Slf4jAntLoggerAdapter antLogger = new Slf4jAntLoggerAdapter(logger);
        antLogger.setEmacsMode(true);
        antLogger.setOutputPrintStream(System.out);
        antLogger.setErrorPrintStream(System.err);

        if (logger.isDebugEnabled()) {
            antLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
        } else {
            antLogger.setMessageOutputLevel(Project.MSG_INFO);
        }

        ant.addBuildListener(antLogger);
    }

    public void setProperty(final String name, Object value) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(value);

        String valueAsString = String.valueOf(value);

        Property prop = (Property) createTask("property");
        prop.setName(name);
        prop.setValue(valueAsString);
        prop.execute();
    }

    public Task createTask(final String name) throws BuildException {
        Preconditions.checkNotNull(name);
        return ant.createTask(name);
    }

    public <T extends ProjectComponent> T createTask(final Class<T> type) {
        Preconditions.checkNotNull(type);

        T task = null;
        try {
            task = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        task.setProject(ant);
        return task;
    }

    public void mkdir(final File dir) {
        Preconditions.checkNotNull(dir);
        Mkdir mkdir = createTask(Mkdir.class);
        mkdir.setDir(dir);
        mkdir.execute();
    }

    public void chmod(final File dir, final String includes, final String perm) {
        Chmod chmod = createTask(Chmod.class);
        chmod.setDir(dir);
        chmod.setIncludes(includes);
        chmod.setPerm(perm);
        chmod.execute();
    }


    /**
     * Adapts Ant logging to Slf4j Logging.
     */
    private static class Slf4jAntLoggerAdapter
            extends DefaultLogger {

        protected Logger logger;

        public Slf4jAntLoggerAdapter() {
            this(null);
        }

        public Slf4jAntLoggerAdapter(final Logger logger) {
            if (logger == null) {
                this.logger = LoggerFactory.getLogger(Slf4jAntLoggerAdapter.class);
            } else {
                this.logger = logger;
            }
        }


        @Override
        protected void printMessage(final String message, final PrintStream stream, final int priority) {
            Preconditions.checkNotNull(message);
            Preconditions.checkNotNull(stream);

            switch (priority) {
                case Project.MSG_ERR:
                    logger.error(message);
                    break;

                case Project.MSG_WARN:
                    logger.warn(message);
                    break;

                case Project.MSG_INFO:
                    logger.info(message);
                    break;

                case Project.MSG_VERBOSE:
                case Project.MSG_DEBUG:
                    logger.debug(message);
                    break;
            }
        }
    }
}
