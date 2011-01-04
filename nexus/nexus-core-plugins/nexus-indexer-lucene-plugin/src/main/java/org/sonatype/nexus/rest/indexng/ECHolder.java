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
package org.sonatype.nexus.rest.indexng;

/**
 * A simple class holding Extension (non-null) and Classifier (may be null).
 * 
 * @author cstamas
 */
public class ECHolder
{
    private final String extension;

    private final String classifier;

    public ECHolder( String extension, String classifier )
    {
        assert extension != null : "Extension cannot be null!";

        this.extension = extension;
        this.classifier = classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    // ==

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( classifier == null ) ? 0 : classifier.hashCode() );
        result = prime * result + ( ( extension == null ) ? 0 : extension.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ECHolder other = (ECHolder) obj;
        if ( classifier == null )
        {
            if ( other.classifier != null )
                return false;
        }
        else if ( !classifier.equals( other.classifier ) )
            return false;
        if ( extension == null )
        {
            if ( other.extension != null )
                return false;
        }
        else if ( !extension.equals( other.extension ) )
            return false;
        return true;
    }
}
