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
package org.sonatype.nexus.proxy.attributes.perf.internal;

import org.sonatype.nexus.proxy.item.DefaultRepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.uid.Attribute;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 *
 */
public class TestRepositoryItemUid
    extends DefaultRepositoryItemUid
{
    public TestRepositoryItemUid( RepositoryItemUidFactory factory, Repository repository, String path )
    {
        super( factory, repository, path );
    }

    @Override
    public <A extends Attribute<V>, V> V getAttributeValue( Class<A> attrClass )
    {
        if ( IsMetadataMaintainedAttribute.class.getName().equals( attrClass.getName() ) )
        {
            return (V) Boolean.TRUE;
        }
        else
        {
            return super.getAttributeValue( attrClass );
        }
    }
}
