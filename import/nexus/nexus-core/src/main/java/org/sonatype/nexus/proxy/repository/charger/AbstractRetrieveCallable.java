/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository.charger;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.common.base.Preconditions;

/**
 * Callable that retrieves an item from a repository, as part of a group request. Used in
 * org.sonatype.nexus.proxy.repository.AbstractGroupRepository.doRetrieveItem(ResourceStoreRequest). All exceptions are
 * supressed by this same class (it implements ExceptionHandler).
 * 
 * @author cstamas
 * @since 2.1
 */
public abstract class AbstractRetrieveCallable<I extends StorageItem>
    implements Callable<I>
{
    private final Logger logger;

    private final Repository repository;

    private final ResourceStoreRequest request;

    private Throwable processingException;

    public AbstractRetrieveCallable( final Logger logger, final Repository repository,
                                     final ResourceStoreRequest request )
    {
        this.logger = Preconditions.checkNotNull( logger );
        this.repository = Preconditions.checkNotNull( repository );
        this.request = Preconditions.checkNotNull( request );
        this.processingException = null;
    }

    public Throwable getProcessingException()
    {
        return processingException;
    }

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    protected Repository getRepository()
    {
        return repository;
    }

    protected ResourceStoreRequest getRequest()
    {
        return request;
    }

    protected void setProcessingException( final Throwable processingException )
    {
        this.processingException = processingException;
    }
}
