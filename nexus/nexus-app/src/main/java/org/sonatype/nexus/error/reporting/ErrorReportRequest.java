/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.error.reporting;

import java.util.HashMap;
import java.util.Map;

public class ErrorReportRequest
{
    private String title;
    
    private String description;
    
    private Throwable throwable;
    
    private Map<String,Object> context = new HashMap<String,Object>();
    
    public Throwable getThrowable()
    {
        return throwable;
    }
    
    public void setThrowable( Throwable throwable )
    {
        this.throwable = throwable;
    }
    
    public Map<String, Object> getContext()
    {
        return context;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setDescription( String description )
    {
        this.description = description;
    }
    
    /**
     * if a title is set, the throwable will not be used, as manual
     * submission is assumed
     * 
     * @param title
     */
    public void setTitle( String title )
    {
        this.title = title;
    }
}
