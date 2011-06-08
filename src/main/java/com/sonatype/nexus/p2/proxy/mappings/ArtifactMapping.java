/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.proxy.mappings;

import java.util.Map;

public class ArtifactMapping
{
    private Map<String, ArtifactPath> artifactsPath;

    private String repository;

    public ArtifactMapping( String repository, Map<String, ArtifactPath> artifactsPath )
    {
        super();
        this.repository = repository;
        this.artifactsPath = artifactsPath;
    }

    public Map<String, ArtifactPath> getArtifactsPath()
    {
        return artifactsPath;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setArtifactsPath( Map<String, ArtifactPath> artifactsPath )
    {
        this.artifactsPath = artifactsPath;
    }

    public void setRepository( String repository )
    {
        this.repository = repository;
    }
}
