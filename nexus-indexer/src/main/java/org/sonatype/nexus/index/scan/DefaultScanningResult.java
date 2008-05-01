/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.index.scan;

import java.util.List;
import java.util.ArrayList;

/** @author Jason van Zyl */
public class DefaultScanningResult
    implements ScanningResult
{
    private int totalFiles;

    private List<Exception> exceptions;

    public DefaultScanningResult()
    {
        exceptions = new ArrayList<Exception>();
    }

    public void setTotalFiles( int totalFiles )
    {
        this.totalFiles = totalFiles;
    }

    public int getTotalFiles()
    {
        return totalFiles;
    }

    public void addException( Exception e )
    {
        exceptions.add( e );
    }

    public boolean hasExceptions()
    {
        return exceptions.size() != 0;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public void incrementCount()
    {
        totalFiles++;
    }
}
