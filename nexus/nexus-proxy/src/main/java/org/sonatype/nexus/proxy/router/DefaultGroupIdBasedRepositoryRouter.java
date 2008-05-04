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
package org.sonatype.nexus.proxy.router;

import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * The default grouping router, without any specific postprocessing.
 * 
 * @author cstamas
 * @plexus.component role-hint="groups"
 */
public class DefaultGroupIdBasedRepositoryRouter
    extends GroupIdBasedRepositoryRouter
{
    public static final String ID = "groups";
    
    private ContentClass contentClass = new DefaultContentClass();

    public String getId()
    {
        return ID;
    }

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }
}
