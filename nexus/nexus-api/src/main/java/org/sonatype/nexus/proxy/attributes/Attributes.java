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

import java.util.Map;

/**
 * Attributes are simply a String key-value pairs with some type-safe getters and setters for keys known and used in
 * core.
 * 
 * @author cstamas
 */
public interface Attributes
    extends Map<String, String>
{
    void overlayAttributes( final Attributes repositoryItemAttributes );

    int getGeneration();

    void setGeneration( final int value );

    void incrementGeneration();

    String getPath();

    void setPath( final String value );

    boolean isReadable();

    void setReadable( final boolean value );

    boolean isWritable();

    void setWritable( final boolean value );

    String getRepositoryId();

    void setRepositoryId( final String value );

    long getCreated();

    void setCreated( final long value );

    long getModified();

    void setModified( final long value );

    long getStoredLocally();

    void setStoredLocally( final long value );

    long getCheckedRemotely();

    void setCheckedRemotely( final long value );

    long getLastRequested();

    void setLastRequested( final long value );

    boolean isExpired();

    void setExpired( final boolean value );

    String getRemoteUrl();

    void setRemoteUrl( final String value );

    long getLength();

    void setLength( final long value );
}
