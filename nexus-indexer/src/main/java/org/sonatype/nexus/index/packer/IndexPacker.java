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
package org.sonatype.nexus.index.packer;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A component that creates defined ZIP and Properties file where the index will get packed.
 * 
 * @author cstamas
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
    void packIndex( IndexingContext context, File targetDir )
        throws IOException,
            IllegalArgumentException;

}
