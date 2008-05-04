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
package org.sonatype.nexus.configuration.upgrade;

/**
 * An upgrade message used to hold the current version and the configuration itself. Since modello generated classes
 * differs from model version to model version, it is held as Object, and the needed converter will cast it to what it
 * needs.
 * 
 * @author cstamas
 */
public class UpgradeMessage
{
    private String modelVersion;

    private Object configuration;

    public String getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion( String modelVersion )
    {
        this.modelVersion = modelVersion;
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
