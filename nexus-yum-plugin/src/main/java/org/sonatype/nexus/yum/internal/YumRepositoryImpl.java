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
package org.sonatype.nexus.yum.internal;

import java.io.File;

import org.sonatype.nexus.yum.YumRepository;

public class YumRepositoryImpl
    implements YumRepository
{

    private final File baseDir;

    private boolean dirty = false;

    private final String version;

    private final String id;

    public YumRepositoryImpl( final File baseDir, final String repositoryId, final String version )
    {
        this.baseDir = baseDir;
        this.id = repositoryId;
        this.version = version;
    }

    @Override
    public File getBaseDir()
    {
        return baseDir;
    }

    @Override
    public File getFile( String path )
    {
        return ( path == null ) ? baseDir : new File( baseDir, path );
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    public void setDirty()
    {
        this.dirty = true;
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public String getId()
    {
        return id;
    }

}
