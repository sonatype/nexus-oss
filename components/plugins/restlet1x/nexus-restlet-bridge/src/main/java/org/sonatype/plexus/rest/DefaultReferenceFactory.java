/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.rest;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * NOTE: this should NOT be a PLEXUS component. If someone wants to load it by creating a config thats great. Providing
 * a default implementation my cause other issues when trying to load an actual implementation. Classes extending this
 * should be able to just override <code>getContextRoot</code>.
 */
public class DefaultReferenceFactory
    implements ReferenceFactory
{

    /**
     * Centralized, since this is the only "dependent" stuff that relies on knowledge where restlet.Application is
     * mounted (we had a /service => / move).
     * 
     * @param request
     * @return
     */
    public Reference getContextRoot( Request request )
    {
        Reference result;

        // if ( getNexus().isForceBaseUrl() && getNexus().getBaseUrl() != null )
        // {
        // result = new Reference( getNexus().getBaseUrl() );
        // }
        // else
        // {
        result = request.getRootRef();
        // }

        // fix for when restlet is at webapp root
        if ( StringUtils.isEmpty( result.getPath() ) )
        {
            result.setPath( "/" );
        }

        return result;
    }

    private Reference updateBaseRefPath( Reference reference )
    {
        if ( reference.getBaseRef().getPath() == null )
        {
            reference.getBaseRef().setPath( "/" );
        }
        else if ( !reference.getBaseRef().getPath().endsWith( "/" ) )
        {
            reference.getBaseRef().setPath( reference.getBaseRef().getPath() + "/" );
        }

        return reference;
    }

    public Reference createThisReference( Request request )
    {
        String uriPart =
            request.getResourceRef().getTargetRef().toString().substring(
                request.getRootRef().getTargetRef().toString().length() );

        // trim leading slash
        if ( uriPart.startsWith( "/" ) )
        {
            uriPart = uriPart.substring( 1 );
        }

        return updateBaseRefPath( new Reference( getContextRoot( request ), uriPart ) );
    }

    public Reference createChildReference( Request request, String childPath )
    {
        Reference result = createThisReference( request ).addSegment( childPath );

        if ( result.hasQuery() )
        {
            result.setQuery( null );
        }

        return result.getTargetRef();
    }

    public Reference createReference( Reference base, String relPart )
    {
        Reference ref = new Reference( base, relPart );

        return updateBaseRefPath( ref ).getTargetRef();
    }

    public Reference createReference( Request base, String relPart )
    {
        return createReference( getContextRoot( base ), relPart );
    }

}
