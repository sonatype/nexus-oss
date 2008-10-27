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
package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.router.DefaultGroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * Mavenized version of RepoGrouId based router. The only difference with the base class is the maven specific
 * aggregation. Since requests may hit multiple resources in groups with multiple repositories, this is the place where
 * we aggregate them. Aggregation happens for repository metadata only.
 * 
 * @author cstamas
 */
@Component( role = RepositoryRouter.class, hint = "groups-m1" )
public class M1GroupIdBasedRepositoryRouter
    extends DefaultGroupIdBasedRepositoryRouter
{
    /**
     * The ContentClass.
     */
    @Requirement( hint = "maven1" )
    private ContentClass contentClass;

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }
}
