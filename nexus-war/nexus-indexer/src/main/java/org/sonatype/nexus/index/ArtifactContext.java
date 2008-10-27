/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;

/**
 * The context of an artifact.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
public class ArtifactContext
{
    private File pom;

    private File artifact;

    private File metadata;

    private ArtifactInfo artifactInfo;

    private Gav gav;

    public ArtifactContext( File pom, File artifact, File metadata, ArtifactInfo artifactInfo, Gav gav )
    {
        this.pom = pom;
        this.artifact = artifact;
        this.metadata = metadata;
        this.artifactInfo = artifactInfo;
        this.gav = gav;
    }

    public File getPom()
    {
        return pom;
    }

    public File getArtifact()
    {
        return artifact;
    }

    public File getMetadata()
    {
        return metadata;
    }

    public ArtifactInfo getArtifactInfo()
    {
        return artifactInfo;
    }

    public Gav getGav()
    {
        return gav;
    }

    public void setGav( Gav gav )
    {
        this.gav = gav;
    }
}
