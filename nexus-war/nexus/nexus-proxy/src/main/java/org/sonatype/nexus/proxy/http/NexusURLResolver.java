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
package org.sonatype.nexus.proxy.http;

import java.net.URL;

/**
 * This component resolves full URLs against known Nexus repositories.
 * 
 * @author cstamas
 */
public interface NexusURLResolver
{
    String ROLE = NexusURLResolver.class.getName();

    /**
     * Resolves the URL to a Nexus URL. The strategy how is it done and to what is it resolved is left to
     * implementation. The result -- if it is not null -- is a Nexus URL from where it is possible to get the artifact
     * addressed with the input URL.
     * 
     * @param url
     * @return the resolved Nexus URL or null if the URL is not resolvable by this resolver.
     */
    URL resolve( URL url );
}
