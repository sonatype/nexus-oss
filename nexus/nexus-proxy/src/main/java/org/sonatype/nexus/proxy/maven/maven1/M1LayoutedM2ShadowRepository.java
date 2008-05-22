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

import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * A shadow repository that transforms M2 layout of master to M1 layouted shadow.
 * 
 * @author cstamas
 * @plexus.component instantiation-strategy="per-lookup" role="org.sonatype.nexus.proxy.repository.Repository"
 *                   role-hint="m2-m1-shadow"
 */
public class M1LayoutedM2ShadowRepository
    extends LayoutConverterShadowRepository
{
    /**
     * The ContentClass.
     * 
     * @plexus.requirement role-hint="maven1"
     */
    private ContentClass contentClass;

    /**
     * The ContentClass.
     * 
     * @plexus.requirement role-hint="maven2"
     */
    private ContentClass masterContentClass;

    /**
     * This repo provides Maven1 content.
     */
    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return getM1GavCalculator();
    }

    /**
     * This repo needs Maven2 content master.
     */
    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    protected String transformMaster2Shadow( String path )
        throws ItemNotFoundException
    {
        return transformM2toM1( path );
    }

    protected String transformShadow2Master( String path )
        throws ItemNotFoundException
    {
        return transformM1toM2( path );
    }

}
