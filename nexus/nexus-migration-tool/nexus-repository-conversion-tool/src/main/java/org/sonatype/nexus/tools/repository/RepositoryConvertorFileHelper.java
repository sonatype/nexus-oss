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

package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Juven Xu
 *
 */
public interface RepositoryConvertorFileHelper
{
    /**
     * move a file or folder to the target location, based on the basePath
     * 
     * @param file The file or folder to be moved
     * @param target The target repository
     * @param basePath The path based on which to run the moving.
     * @throws IOException
     */
    void move( File file, File target, String basePath )
        throws IOException;

    /**
     * copy a file or folder to the target location, based on the basePath
     * 
     * @param file The file or folder to be moved
     * @param target The target repository
     * @param basePath The path based on which to run the moving.
     * @throws IOException
     */
    void copy( File file, File target, String basePath )
        throws IOException;
}
