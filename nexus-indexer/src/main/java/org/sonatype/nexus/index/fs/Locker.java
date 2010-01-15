/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.fs;

import java.io.File;
import java.io.IOException;

/**
 * Filesystem locker. Can be used to synchronise access to filesystem directories
 * from different operating system processes.  
 * 
 * @author igor
 */
public interface Locker
{
    String LOCK_FILE = ".lock";

    /**
     * Acquires exclusive lock on specified directory. Most implementation will
     * use marker file and will only work if all processes that require access
     * to the directory use the same filename.
     */
    Lock lock( File directory )
        throws IOException;
}
