/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

public class DefaultRemoteConnectionSettings
    implements RemoteConnectionSettings
{
    private int connectionTimeout = 1000;

    private int retrievalRetryCount = 3;

    private String queryString;

    private String userAgentCustomizationString;

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout( int connectionTimeout )
    {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRetrievalRetryCount()
    {
        return retrievalRetryCount;
    }

    public void setRetrievalRetryCount( int retrievalRetryCount )
    {
        this.retrievalRetryCount = retrievalRetryCount;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setQueryString( String queryString )
    {
        this.queryString = queryString;
    }

    public String getUserAgentCustomizationString()
    {
        return userAgentCustomizationString;
    }

    public void setUserAgentCustomizationString( String userAgentCustomizationString )
    {
        this.userAgentCustomizationString = userAgentCustomizationString;
    }
}
