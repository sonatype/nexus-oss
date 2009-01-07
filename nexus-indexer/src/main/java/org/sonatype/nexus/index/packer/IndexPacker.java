/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.packer;

import java.io.IOException;

/**
 * A component that creates defined ZIP and Properties file where the index will get packed.
 * 
 * @author Tamas Cservenak
 */
public interface IndexPacker
{
    String ROLE = IndexPacker.class.getName();

    /**
     * Pack a context into a target directory. If the directory does not exists, it will be created. If the directory
     * exists, it should be writable.
     * 
     * @param context the context to pack-up
     * @param targetDir the directory where to write results, has to be non-null
     * @throws IllegalArgumentException when the targetDir already exists and is not a writable directory.
     * @throws IOException on lethal IO problem
     */
    void packIndex( IndexPackingRequest request )
        throws IOException,
            IllegalArgumentException;

}
