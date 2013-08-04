/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.item;

import java.io.InputStreamReader;
import java.io.StringWriter;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.velocity.Velocity;

import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;

@Component(role = ContentGenerator.class, hint = VelocityContentGenerator.ID)
public class VelocityContentGenerator
    implements ContentGenerator
{
  public static final String ID = "velocity";

  @Requirement
  private Velocity velocity;

  @Override
  public String getGeneratorId() {
    return ID;
  }

  @Override
  public ContentLocator generateContent(Repository repository, String path, StorageFileItem item)
      throws IllegalOperationException, ItemNotFoundException, LocalStorageException
  {
    InputStreamReader isr = null;

    try {
      StringWriter sw = new StringWriter();
      VelocityContext vctx = new VelocityContext(item.getItemContext());
      isr = new InputStreamReader(item.getInputStream(), "UTF-8");
      velocity.getEngine().evaluate(vctx, sw, item.getRepositoryItemUid().toString(), isr);
      return new StringContentLocator(sw.toString());
    }
    catch (Exception e) {
      throw new LocalStorageException("Could not expand the template: " + item.getRepositoryItemUid().toString(), e);
    }
    finally {
      IOUtil.close(isr);
    }
  }
}
