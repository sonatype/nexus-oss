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
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractProxyRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String PROXY_MODE = "proxyMode";

    private static final String REMOTE_STATUS_CHECK_MODE = "remoteStatusCheckMode";

    private static final String ITEM_MAX_AGE = "itemMaxAge";

    private static final String ITEM_AGING_ACTIVE = "itemAgingActive";

    private static final String AUTO_BLOCK_ACTIVE = "autoBlockActive";
    
    public static final String FILE_TYPE_VALIDATION = "fileTypeValidation";

    public AbstractProxyRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public ProxyMode getProxyMode()
    {
        return ProxyMode.valueOf( getNodeValue( getRootNode(), PROXY_MODE, ProxyMode.ALLOW.toString() ) );
    }

    public void setProxyMode( ProxyMode mode )
    {
        setNodeValue( getRootNode(), PROXY_MODE, mode.toString() );
    }
    

    public boolean isFileTypeValidation()
    {
        return Boolean.valueOf( getNodeValue( getRootNode(), FILE_TYPE_VALIDATION, "true" ) );
    }

    public void setFileTypeValidation( boolean doValidate )
    {
        setNodeValue( getRootNode(), FILE_TYPE_VALIDATION, Boolean.toString( doValidate ) );
    }

    public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
    {
        return RepositoryStatusCheckMode.valueOf( getNodeValue( getRootNode(), REMOTE_STATUS_CHECK_MODE,
            RepositoryStatusCheckMode.AUTO_BLOCKED_ONLY.toString() ) );
    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        setNodeValue( getRootNode(), REMOTE_STATUS_CHECK_MODE, mode.toString() );
    }

    public int getItemMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), ITEM_MAX_AGE, "1440" ) );
    }

    public void setItemMaxAge( int age )
    {
        setNodeValue( getRootNode(), ITEM_MAX_AGE, String.valueOf( age ) );
    }

    public boolean isItemAgingActive()
    {
        return Boolean.parseBoolean( getNodeValue( getRootNode(), ITEM_AGING_ACTIVE, Boolean.TRUE.toString() ) );
    }

    public void setItemAgingActive( boolean value )
    {
        setNodeValue( getRootNode(), ITEM_AGING_ACTIVE, Boolean.toString( value ) );
    }

    public boolean isAutoBlockActive()
    {
        return Boolean.parseBoolean( getNodeValue( getRootNode(), AUTO_BLOCK_ACTIVE, Boolean.TRUE.toString() ) );
    }

    public void setAutoBlockActive( boolean value )
    {
        setNodeValue( getRootNode(), AUTO_BLOCK_ACTIVE, Boolean.toString( value ) );
    }
}
