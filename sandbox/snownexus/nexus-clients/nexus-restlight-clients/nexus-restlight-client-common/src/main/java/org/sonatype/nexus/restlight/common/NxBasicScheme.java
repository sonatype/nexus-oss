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

import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.BasicScheme;

/**
 * {@link AuthScheme} for use with commons-httpclient that implements Nexus' NxBASIC
 * HTTP authentication scheme. This is just an extension of {@link BasicScheme} that uses the name
 * 'NxBASIC' for registration with httpclient.
 */
public class NxBasicScheme
    extends BasicScheme
{

    static final String NAME = "NxBASIC";

    @Override
    public String getSchemeName()
    {
        return NAME;
    }

    @Override
    public String getID()
    {
        return NAME;
    }
}
