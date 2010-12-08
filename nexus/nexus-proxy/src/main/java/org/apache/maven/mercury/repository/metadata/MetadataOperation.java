/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.maven.mercury.repository.metadata;

import org.apache.maven.artifact.repository.metadata.Metadata;

/**
 * change of a Metadata object
 *
 * @author Oleg Gusakov
 * @version $Id: MetadataOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 *
 */
public interface MetadataOperation
{
    /**
     *  sets the operation's data
     */
    public void setOperand( Object data )
        throws MetadataException;
  
    /**
     * performs the operation
     * 
     * @param metadata to perform on
     * @return true if operation changed the data
     */
    public boolean perform( Metadata metadata )
        throws MetadataException;
}
