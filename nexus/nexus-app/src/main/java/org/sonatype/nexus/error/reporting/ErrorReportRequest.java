/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
