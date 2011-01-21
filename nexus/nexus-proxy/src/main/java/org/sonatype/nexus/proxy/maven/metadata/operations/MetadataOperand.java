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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.apache.maven.artifact.repository.metadata.Metadata;

/**
 * @author Oleg Gusakov
 * @version $Id: MetadataOperand.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class MetadataOperand
    extends AbstractOperand
{
    private final Metadata metadata;

    public MetadataOperand( final Metadata data )
    {
        super( data == null ? ModelVersionUtility.LATEST_MODEL_VERSION : ModelVersionUtility.getModelVersion( data ) );

        if ( data == null )
        {
            this.metadata = new Metadata();
        }
        else
        {
            this.metadata = data;
        }
    }

    public Metadata getOperand()
    {
        return metadata;
    }
}
