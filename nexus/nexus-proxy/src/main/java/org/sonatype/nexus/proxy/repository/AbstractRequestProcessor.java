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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;

/**
 * A helper base class that makes it easier to create processors. Note: despite it's name, this class is not abstract
 * class.
 * 
 * @author cstamas
 */
public abstract class AbstractRequestProcessor
    implements RequestProcessor
{
    public boolean process( Repository repository, ResourceStoreRequest request, Action action )
    {
        return true;
    }

    public boolean shouldProxy( ProxyRepository proxy, ResourceStoreRequest request )
    {
        return true;
    }

    public boolean shouldCache( ProxyRepository proxy, AbstractStorageItem item )
    {
        return true;
    }
}
