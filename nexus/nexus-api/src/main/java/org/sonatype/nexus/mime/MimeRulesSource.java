/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.mime;

/**
 * Interface to provide "hints" what a given source thinks about MIME type of some items.
 * 
 * @author cstamas
 */
public interface MimeRulesSource
{
    MimeRulesSource NOOP = new MimeRulesSource()
    {
        @Override
        public String getRuleForPath( final String path )
        {
            return null;
        }
    };

    /**
     * Returns the forced MIME type that corresponds (should correspond) to given path in the context of given rule
     * source. Returns {@code null} if no rules found.
     * 
     * @param path
     * @return
     */
    String getRuleForPath( String path );
}
