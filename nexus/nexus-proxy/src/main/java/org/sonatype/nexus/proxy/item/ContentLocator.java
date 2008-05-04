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
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Interface ContentLocator. Implements a strategy to fetch content of a file item.
 * 
 * @author cstamas
 */
public interface ContentLocator
{

    /**
     * Gets the content.
     * 
     * @return the content
     * @throws IOException Signals that an I/O exception has occurred.
     */
    InputStream getContent()
        throws IOException;

    /**
     * Checks if is reusable.
     * 
     * @return true, if is reusable
     */
    boolean isReusable();

}
