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
package org.sonatype.nexus.configuration.validator;

/**
 * A request for validation, holding the configuration.
 * 
 * @author cstamas
 */
public class ValidationRequest
{
    /**
     * The configuration to validate.
     */
    private Object configuration;

    public ValidationRequest( Object configuration )
    {
        super();

        this.configuration = configuration;
    }

    public Object getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Object configuration )
    {
        this.configuration = configuration;
    }
}
