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

/**
 * Convert a repository mixed with release and snapshot versions to two, one with release versions and the other with
 * snapshot version.</br> For example, a repository named 'third-party' will be converted to 'third-party-releases' and
 * 'third-party-snapshots'.
 * 
 * @author Juven Xu
 */
public interface RepositoryConvertor
{
    String SUFFIX_RELEASES = "-releases";
    
    String SUFFIX_SNAPSHOTS = "-snapshots"; 
    
    void convertRepository(File repository, File targetPath);
}
