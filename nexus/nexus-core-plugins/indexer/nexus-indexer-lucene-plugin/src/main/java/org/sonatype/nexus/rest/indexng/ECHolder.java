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
