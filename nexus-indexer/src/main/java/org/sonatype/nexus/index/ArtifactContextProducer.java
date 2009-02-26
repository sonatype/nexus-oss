/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A producer that creates {@link ArtifactContext} from POM and from other available files.
 * 
 * @author Tamas Cservenak
 */
public interface ArtifactContextProducer
{
    String ROLE = ArtifactContextProducer.class.getName();

    public ArtifactContext getArtifactContext( IndexingContext context, File file );

}
