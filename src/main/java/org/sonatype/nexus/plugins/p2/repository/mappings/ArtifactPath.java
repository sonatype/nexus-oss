/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.mappings;

public class ArtifactPath
{
    private String md5;

    private String path;

    public ArtifactPath( String path, String md5 )
    {
        super();
        this.path = path;
        this.md5 = md5;
    }

    public String getMd5()
    {
        return md5;
    }

    public String getPath()
    {
        return path;
    }

    public void setMd5( String md5 )
    {
        this.md5 = md5;
    }

    public void setPath( String path )
    {
        this.path = path;
    }
}
