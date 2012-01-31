/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.configuration.upgrade;

/**
 * An upgrade message used to hold the current version and the configuration itself. Since modello generated classes
 * differs from model version to model version, it is held as Object, and the needed converter will cast it to what it
 * needs.
 * 
 * @author cstamas
 */
public class UpgradeMessage
{
    private String modelVersion;

    private Object configuration;

    public String getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion( String modelVersion )
    {
        this.modelVersion = modelVersion;
    }

    public Object getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Object configuration )
    {
        this.configuration = configuration;
    }
}
