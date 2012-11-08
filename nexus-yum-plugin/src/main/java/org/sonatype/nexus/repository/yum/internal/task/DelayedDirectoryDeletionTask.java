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
package org.sonatype.nexus.repository.yum.internal.task;

import org.sonatype.nexus.plugins.yum.plugin.DeletionService;
import org.sonatype.nexus.proxy.repository.Repository;

public class DelayedDirectoryDeletionTask
    implements Runnable
{

    private final DeletionService service;

    private final Repository repository;

    private final String path;

    private boolean active = false;

    private int executionCount = 0;

    public DelayedDirectoryDeletionTask( DeletionService service, Repository repository, String path )
    {
        this.service = service;
        this.repository = repository;
        this.path = path + "/";
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    @Override
    public void run()
    {
        executionCount++;
        service.execute( this );
    }

    public boolean isParent( Repository repo, String subPath )
    {
        return repository.getId().equals( repository.getId() ) && subPath.startsWith( path );
    }

    public Repository getRepository()
    {
        return repository;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getPath()
    {
        return path;
    }

    public int getExecutionCount()
    {
        return executionCount;
    }
}
