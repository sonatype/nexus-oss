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
package org.sonatype.nexus.proxy.storage.remote;

/**
 * Component that drives the remote storage transport provider selection by telling the "hint" (former Plexus role
 * hint), of the RRS component to be used. It allows multiple way of configuration, either by setting the default
 * provider and even forceful overriding the hint (if configuration would say otherwise). This component is meant to
 * help smooth transition from Apache HttpClient3x RRS (for long time the one and only RRS implementation) to Ning's
 * AsyncHttpClient implementation.
 */
public interface RemoteProviderHintFactory
{
    /**
     * Returns the default provider role hint for provided remote URL.
     * 
     * @return The default provider role hint as a string.
     */
    String getDefaultRoleHint( final String remoteUrl )
        throws IllegalArgumentException;

    /**
     * Returns the provider role hint to be used, based on passed in remote URL and hint.
     * 
     * @return The provider role hint to be used, based on passed in remote URL and hint. If forceful override is in
     *         effect, it will return the forced, otherwise the passed in one (if it is valid, non-null, etc).
     */
    String getRoleHint( final String remoteUrl, final String hint )
        throws IllegalArgumentException;

    /**
     * Returns the default HTTP provider role hint.
     * 
     * @return The default HTTP provider role hint as a string.
     */
    String getDefaultHttpRoleHint();

    /**
     * Returns the HTTP provider role hint to be used, based on passed in hint.
     * 
     * @return The HTTP provider role hint to be used, based on passed in hint. If forceful override is in effect, it
     *         will return the forced, otherwise the passed in one (if it is valid, non-null, etc).
     */
    String getHttpRoleHint( final String hint );
}
