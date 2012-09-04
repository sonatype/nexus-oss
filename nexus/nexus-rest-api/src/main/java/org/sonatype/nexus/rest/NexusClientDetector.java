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
package org.sonatype.nexus.rest;

import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;

/**
 * Helper class to detect Nexus client.
 * 
 * @author cstamas
 * @since 2.1
 */
public class NexusClientDetector
{
    /**
     * Returns true if client is some sort of "application" (whether standalone or part of some RESTful UI, does not
     * matter): point is that response is not "rendered" directly to user (like an error page in Browser, but should be
     * made available in programmatically reusable way). This is how NexusClient behaves: accepts only XML and nothing
     * else and enforces only UTF8 character encoding and nothing else (client explicitly states this in Accept header).
     * 
     * @param request
     * @return
     */
    public boolean isNexusClientAlike( final Request request )
    {
        final List<Preference<CharacterSet>> acceptedCharacterSets = request.getClientInfo().getAcceptedCharacterSets();
        final List<Preference<MediaType>> acceptedMediaTypes = request.getClientInfo().getAcceptedMediaTypes();
        if ( acceptedCharacterSets.size() == 1
            && CharacterSet.UTF_8.equals( acceptedCharacterSets.get( 0 ).getMetadata() ) )
        {
            if ( acceptedMediaTypes.size() == 1
                && MediaType.APPLICATION_XML.equals( acceptedMediaTypes.get( 0 ).getMetadata(), true ) )
            {
                // this is a client
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if request initiator presents itself as "Nexus-Client" and has all the properties that Nexus client
     * has.
     * 
     * @param request
     * @return
     */
    public boolean isNexusClient( final Request request )
    {
        return isNexusClientAlike( request ) && request.getClientInfo().getAgent().startsWith( "Nexus-Client/" );
    }
}
