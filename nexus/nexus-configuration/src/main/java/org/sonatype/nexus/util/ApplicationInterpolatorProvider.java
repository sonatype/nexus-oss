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
package org.sonatype.nexus.util;

import org.codehaus.plexus.util.interpolation.Interpolator;

/**
 * A simple component to centralize Plexus context access for interpolation needs.
 * 
 * @author cstamas
 */
public interface ApplicationInterpolatorProvider
    extends Interpolator
{
    String ROLE = ApplicationInterpolatorProvider.class.getName();

    /**
     * Returns the interpolator.
     * 
     * @return
     */
    Interpolator getInterpolator();
}
