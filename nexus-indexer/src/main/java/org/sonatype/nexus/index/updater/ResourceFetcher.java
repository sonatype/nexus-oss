/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.IOException;

/**
 * An interface defining resource downloading contract
 * 
 * @author Eugene Kuleshov
 */
public interface ResourceFetcher 
{
    void connect( String id, String url ) throws IOException;
    void disconnect() throws IOException;
    void retrieve( String name, File targetFile ) throws IOException;
}
