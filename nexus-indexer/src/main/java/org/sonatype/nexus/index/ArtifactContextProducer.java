/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A producer that creates ArtifactContext from POM (and possibly other available files).
 * 
 * @author cstamas
 */
public interface ArtifactContextProducer
{
    String ROLE = ArtifactContextProducer.class.getName();

    public ArtifactContext getArtifactContext( IndexingContext context, File file );

}
