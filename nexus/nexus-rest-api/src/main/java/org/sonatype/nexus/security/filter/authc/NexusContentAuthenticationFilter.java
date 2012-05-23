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

package org.sonatype.nexus.security.filter.authc;

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.sonatype.inject.Nullable;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.util.List;

/**
 * Nexus {code}/content{code} {@link AuthenticationFilter}.
 *
 * @see NexusContentRestrictionConstituent
 * @see NexusContentRestrictedToken
 *
 * @since 2.1
 */
public class NexusContentAuthenticationFilter
    extends NexusSecureHttpAuthenticationFilter
{
    // NOTE: Using field-injection due to issues with org.codehaus.plexus:plexus-component-metadata:1.5.5 (caused by qdox 1.9.2)
    // NOTE: ... which can't handle annotated parameters apparently.

    @Inject
    @Nullable
    private List<NexusContentRestrictionConstituent> restrictionConstituents;

    public NexusContentAuthenticationFilter()
    {
        super();
    }

    @VisibleForTesting
    public NexusContentAuthenticationFilter( final List<NexusContentRestrictionConstituent> restrictionConstituents )
    {
        this.restrictionConstituents = restrictionConstituents;
    }

    /**
     * Determine if content restriction is enabled, by asking each constituent.
     * If any constituent reports a restriction then returns true.
     */
    private boolean isRestricted()
    {
        //noinspection ConstantConditions
        if ( restrictionConstituents != null ) {
            for ( NexusContentRestrictionConstituent constituent : restrictionConstituents ) {
                if ( constituent.isContentRestricted() ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected AuthenticationToken createToken( final ServletRequest request, final ServletResponse response )
    {
        if ( isRestricted() ) {
            getLogger().debug( "Content authentication is restricted" );

            // We know our super-class makes UsernamePasswordTokens, ask super to pull out the relevant details
            UsernamePasswordToken basis = ( UsernamePasswordToken ) super.createToken( request, response );

            // And include more information than is normally provided to a token (ie. the request)
            return new NexusContentRestrictedToken( basis, request );
        }
        else {
            return super.createToken( request, response );
        }
    }
}
