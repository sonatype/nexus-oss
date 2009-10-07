/*
 * Nexus: RESTLight Client
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.restlight.common;

/**
 * Exception indicating a failure to communicate with the Nexus server. This normally means either an 
 * I/O failure or a failure to parse the response message.
 */
public class RESTLightClientException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    public RESTLightClientException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public RESTLightClientException( final String message )
    {
        super( message );
    }

}
