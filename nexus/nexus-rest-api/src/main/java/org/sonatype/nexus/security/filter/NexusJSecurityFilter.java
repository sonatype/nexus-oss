/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.jsecurity.web.PlexusJSecurityFilter;
import org.sonatype.nexus.Nexus;

/**
 * This filter simply behaves according Nexus configuration.
 * 
 * @author cstamas
 */
public class NexusJSecurityFilter
    extends PlexusJSecurityFilter
{
    public static final String REQUEST_IS_AUTHZ_REJECTED = "request.is.authz.rejected";

    private Nexus nexus;
    
    public NexusJSecurityFilter()
    {
        // not setting configClassName explicitly, so we can use either configRole or configClassName
    }

    protected final Nexus getNexus()
    {
        if ( nexus == null )
        {
            try
            {
                nexus = (Nexus) getPlexusContainer().lookup( Nexus.class );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot lookup Nexus!", e );
            }
        }

        return nexus;
    }

    @Override
    protected boolean shouldNotFilter( ServletRequest request )
        throws ServletException
    {
        return !getNexus().isSecurityEnabled();
    }

    @Override
    protected void doFilterInternal( ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain origChain )
        throws ServletException,
            IOException
    {
        servletRequest.setAttribute( Nexus.class.getName(), getNexus() );

        super.doFilterInternal( servletRequest, servletResponse, origChain );
    }
}
