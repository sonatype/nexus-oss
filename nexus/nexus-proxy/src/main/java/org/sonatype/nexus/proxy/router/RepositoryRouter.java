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

import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Repository Router interface. This router offers a simple API to request items from Proximity. It calculates and
 * knows which repositories are registered within Proximity.
 * 
 * @see Repository
 * @see RepositoryRegistry
 * @author cstamas
 */
public interface RepositoryRouter
    extends ResourceStore, ConfigurationChangeListener
{
    String ROLE = RepositoryRouter.class.getName();

    /**
     * The content class that is handled by this router.
     * 
     * @return
     */
    ContentClass getHandledContentClass();
}
