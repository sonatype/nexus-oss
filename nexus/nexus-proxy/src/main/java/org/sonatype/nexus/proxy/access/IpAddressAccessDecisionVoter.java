/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class IpAddressAccessDecisionVoter.
 * 
 * @author cstamas
 * @plexus.component role-hint="ip-based"
 */
public class IpAddressAccessDecisionVoter
    implements AccessDecisionVoter
{

    public static final String REQUEST_REMOTE_ADDRESS = "requestRemoteAddress";

    /** The allow from pattern. */
    private String allowFromPattern;

    /** The deny from pattern. */
    private String denyFromPattern;

    /** The allow deny. */
    private boolean allowDeny;

    /** The allow from. */
    private Pattern allowFrom;

    /** The deny from. */
    private Pattern denyFrom;

    /**
     * Checks if is allow deny.
     * 
     * @return true, if is allow deny
     */
    public boolean isAllowDeny()
    {
        return allowDeny;
    }

    /**
     * Sets the allow deny.
     * 
     * @param allowDeny the new allow deny
     */
    public void setAllowDeny( boolean allowDeny )
    {
        this.allowDeny = allowDeny;
    }

    /**
     * Gets the allow from pattern.
     * 
     * @return the allow from pattern
     */
    public String getAllowFromPattern()
    {
        return allowFromPattern;
    }

    /**
     * Sets the allow from pattern.
     * 
     * @param allowFromPattern the new allow from pattern
     */
    public void setAllowFromPattern( String allowFromPattern )
    {
        this.allowFromPattern = allowFromPattern;
    }

    /**
     * Gets the deny from pattern.
     * 
     * @return the deny from pattern
     */
    public String getDenyFromPattern()
    {
        return denyFromPattern;
    }

    /**
     * Sets the deny from pattern.
     * 
     * @param denyFromPattern the new deny from pattern
     */
    public void setDenyFromPattern( String denyFromPattern )
    {
        this.denyFromPattern = denyFromPattern;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.access.AccessDecisionVoter#vote(org.sonatype.nexus.ProximityRequest,
     *      org.sonatype.nexus.Repository, org.sonatype.nexus.access.RepositoryPermission)
     */
    public int vote( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
    {
        if ( request.getRequestContext().containsKey( REQUEST_REMOTE_ADDRESS )
            && isAccessAllowed( (String) request.getRequestContext().get( REQUEST_REMOTE_ADDRESS ) ) )
        {
            return ACCESS_APPROVED;
        }
        else
        {
            return ACCESS_DENIED;
        }
    }

    /**
     * Checks if is access allowed.
     * 
     * @param ipAddress the ip address
     * @return true, if is access allowed
     */
    private boolean isAccessAllowed( String ipAddress )
    {
        if ( allowFrom == null || denyFrom == null )
        {
            allowFrom = Pattern.compile( this.allowFromPattern );
            denyFrom = Pattern.compile( this.denyFromPattern );
        }
        Matcher allowMatcher = allowFrom.matcher( ipAddress );
        Matcher denyMatcher = denyFrom.matcher( ipAddress );
        if ( isAllowDeny() )
        {
            if ( allowMatcher.matches() )
            {
                return true;
            }
            if ( denyMatcher.matches() )
            {
                return false;
            }
            return false;
        }
        else
        {
            if ( denyMatcher.matches() )
            {
                return false;
            }
            if ( allowMatcher.matches() )
            {
                return true;
            }
            return true;
        }
    }

}
