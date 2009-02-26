/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A scanning result holds result of repository scan
 *  
 * @author Jason van Zyl 
 */
public class ScanningResult
{
    private int totalFiles = 0;
    
    private int deletedFiles = 0;

    private List<Exception> exceptions = new ArrayList<Exception>();

    public void setTotalFiles( int totalFiles )
    {
        this.totalFiles = totalFiles;
    }
    
    public void setDeletedFiles(int deletedFiles) 
    {
        this.deletedFiles = deletedFiles;
    }
    
    public int getTotalFiles()
    {
        return totalFiles;
    }
    
    public int getDeletedFiles() 
    {
        return deletedFiles;
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

}
