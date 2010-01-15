/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An interface defining resource downloading contract
 * 
 * @author Eugene Kuleshov
 */
public interface ResourceFetcher 
{
    /**
     * Connect and start transfer session
     */
    void connect( String id, String url ) throws IOException;
    
    /**
     * Disconnect and complete transfer session
     */
    void disconnect() throws IOException;
    
    /**
     * Retrieves file
     * 
     * @param name a name of resource to retrieve
     * @param targetFile a target file to save retrieved resource to 
     */
    void retrieve( String name, File targetFile ) throws IOException, FileNotFoundException;

    /**
     * Retrieves resource as InputStream
     * 
     * @param name a name of resource to retrieve
     */
    InputStream retrieve( String name ) throws IOException, FileNotFoundException;
}
