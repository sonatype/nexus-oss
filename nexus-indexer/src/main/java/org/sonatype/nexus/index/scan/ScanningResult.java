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
package org.sonatype.nexus.index.scan;

import java.util.List;

/** @author Jason van Zyl */
public interface ScanningResult
{
    int getTotalFiles();

    int getDeletedFiles();

    void setTotalFiles(int count);

    void setDeletedFiles(int count);
    
    void addException( Exception e );
    
    public boolean hasExceptions();

    public List<Exception> getExceptions();

}
