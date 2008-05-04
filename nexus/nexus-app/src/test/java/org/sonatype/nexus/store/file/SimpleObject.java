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
package org.sonatype.nexus.store.file;

public class SimpleObject
{

    private String aString;

    private int anInt;

    private boolean aBoolean;

    public String getAString()
    {
        return aString;
    }

    public void setAString( String string )
    {
        aString = string;
    }

    public int getAnInt()
    {
        return anInt;
    }

    public void setAnInt( int anInt )
    {
        this.anInt = anInt;
    }

    public boolean isABoolean()
    {
        return aBoolean;
    }

    public void setABoolean( boolean boolean1 )
    {
        aBoolean = boolean1;
    }

}
