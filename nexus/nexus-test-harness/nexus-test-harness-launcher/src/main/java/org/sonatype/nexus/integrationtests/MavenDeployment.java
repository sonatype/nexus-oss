/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests;

import java.io.File;

import org.sonatype.nexus.test.utils.TestProperties;

import com.google.common.base.Preconditions;

/**
 * A simple descriptor object describing the maven runtime and maven project you want to run Verifier against.
 * 
 * @author cstamas
 * @since 2.1
 */
public class MavenDeployment
{
    private final File mavenHomeFile;

    private final File localRepositoryFile;

    private final File logFile;

    private final File settingsXmlFile;

    private final File mavenProjectFile;

    /**
     * Creates a new instance of {@link MavenDeployment}.
     * 
     * @param testId the test ID, must not be {@code null}.
     * @param mavenHomeFile the maven home, must not be {@code null}.
     * @param localRepositoryFile the local repository, must not be {@code null}.
     * @param logFile the log file of Verifier, must not be {@code null}.
     * @param settingsXmlFile settings.xml file, must not be {@code null}.
     * @param mavenProjectFile directory containing the project (pom.xml), must not be {@code null}.
     */
    public MavenDeployment( final File mavenHomeFile, final File localRepositoryFile, final File logFile,
                            final File settingsXmlFile, final File mavenProjectFile )
    {
        this.mavenHomeFile = Preconditions.checkNotNull( mavenHomeFile );
        this.localRepositoryFile = Preconditions.checkNotNull( localRepositoryFile );
        this.logFile = Preconditions.checkNotNull( logFile );
        this.settingsXmlFile = Preconditions.checkNotNull( settingsXmlFile );
        this.mavenProjectFile = Preconditions.checkNotNull( mavenProjectFile );
    }

    /**
     * Returns the Maven Home file (directory) where maven deployment is (unpacked binary distro of Maven).
     * 
     * @return
     */
    public File getMavenHomeFile()
    {
        return mavenHomeFile;
    }

    /**
     * Returns the file (directory) where you want to Maven put it's local repository.
     * 
     * @return
     */
    public File getLocalRepositoryFile()
    {
        return localRepositoryFile;
    }

    /**
     * Returns the logfile where you want to have Maven console output saved.
     * 
     * @return
     */
    public File getLogFile()
    {
        return logFile;
    }

    /**
     * Returns the settings.xml file you want to use with Maven.
     * 
     * @return
     */
    public File getSettingsXmlFile()
    {
        return settingsXmlFile;
    }

    /**
     * Retutns the baseDir of maven project you want to run Maven against.
     * 
     * @return
     */
    public File getMavenProjectFile()
    {
        return mavenProjectFile;
    }

    // ==

    /**
     * Returns the default deployment descriptor used throughout ITs. This is just a "handy" quick method that does
     * things in same was as they happened before (pre 2.1).
     * 
     * @param testId
     * @param logFile
     * @param settingsXmlFile
     * @param mavenProject
     * @return
     */
    public static MavenDeployment defaultDeployment( final File logFile, final File settingsXmlFile,
                                                     final File mavenProject )
    {
        return new MavenDeployment( new File( TestProperties.getString( "maven.instance" ) ), new File(
            TestProperties.getString( "maven.local.repo" ) ), logFile, settingsXmlFile, mavenProject );
    }
}
