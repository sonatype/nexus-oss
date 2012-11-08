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
package org.sonatype.nexus.repository.yum.internal.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * Override default maven2 content class to implement compability between
 * {@link org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass} and {@link M2YumContentClass}
 * 
 * @author sherold
 */
@Component( role = ContentClass.class, hint = M2ContentClass.ID )
public class M2ContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "maven2";

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean isCompatible( ContentClass contentClass )
    {
        return ID.equals( contentClass.getId() ) || M2YumContentClass.ID.equals( contentClass.getId() );
    }
}
