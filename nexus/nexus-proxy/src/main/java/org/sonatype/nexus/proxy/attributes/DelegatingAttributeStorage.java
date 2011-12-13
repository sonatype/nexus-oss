/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;

import com.google.common.base.Preconditions;

/**
 * Simple wrapping AttributeStorage that delegates only when needed.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public class DelegatingAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
    private final AttributeStorage delegate;

    public DelegatingAttributeStorage( final AttributeStorage delegate )
    {
        this.delegate = Preconditions.checkNotNull( delegate );
    }

    public AttributeStorage getDelegate()
    {
        return delegate;
    }

    @Override
    public Attributes getAttributes( RepositoryItemUid uid )
        throws IOException
    {
        if ( isMetadataMaintained( uid ) )
        {
            return delegate.getAttributes( uid );
        }

        return null;
    }

    @Override
    public void putAttributes( RepositoryItemUid uid, Attributes attributes )
        throws IOException
    {
        if ( isMetadataMaintained( uid ) )
        {
            delegate.putAttributes( uid, attributes );
        }
    }

    @Override
    public boolean deleteAttributes( RepositoryItemUid uid )
        throws IOException
    {
        if ( isMetadataMaintained( uid ) )
        {
            return delegate.deleteAttributes( uid );
        }

        return false;
    }

    // ==

    /**
     * Returns true if the attributes should be maintained at all.
     * 
     * @param uid
     * @return true if attributes should exists for given UID.
     */
    protected boolean isMetadataMaintained( final RepositoryItemUid uid )
    {
        Boolean isMetadataMaintained = uid.getAttributeValue( IsMetadataMaintainedAttribute.class );

        if ( isMetadataMaintained != null )
        {
            return isMetadataMaintained.booleanValue();
        }
        else
        {
            // safest
            return true;
        }
    }
}
