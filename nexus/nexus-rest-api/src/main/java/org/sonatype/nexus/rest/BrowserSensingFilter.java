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
package org.sonatype.nexus.rest;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * A filter that tries to recognize browsers a "clients", and "fixes" their Acccept headers.
 * 
 * @author cstamas
 * @see http://article.gmane.org/gmane.comp.java.restlet/4205/match=browser
 */
public class BrowserSensingFilter
    extends Filter
{

    /**
     * The filter constructor.
     * 
     * @param context
     */
    public BrowserSensingFilter( Context context )
    {
        super( context );
    }

    /**
     * A beforeHandle will simply embed in request attributes a Nexus interface implemntor, depending on key used to
     * name it.
     */
    protected void beforeHandle( Request request, Response response )
    {
        String agentInfo = request.getClientInfo().getAgent() != null ? request
            .getClientInfo().getAgent().toLowerCase() : "unknown";

        // This solution was the only that came on my mind :)
        // should work only if client specified more then one "alternatives"
        if ( StringUtils.indexOfAny( agentInfo, new String[] { "mozilla", "firefox", "msie", "opera", "safari" } ) > -1
            && request.getClientInfo().getAcceptedMediaTypes().size() > 1 )
        {
            // overriding client preferences, since it is a browser to TEXT/HTML
            // doing this by adding text/html as firxt to accepted media types with quality 1
            request
                .getClientInfo().getAcceptedMediaTypes().add( 0, new Preference<MediaType>( MediaType.TEXT_HTML, 1 ) );
        }
    }

}
