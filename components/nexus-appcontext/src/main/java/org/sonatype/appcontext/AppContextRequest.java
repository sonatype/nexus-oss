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

package org.sonatype.appcontext;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.publisher.EntryPublisher;
import org.sonatype.appcontext.source.EntrySource;

/**
 * Request for building {@link AppContext} instances.
 *
 * @author cstamas
 */
public class AppContextRequest
{
  private final String id;

  private final AppContext parent;

  private final List<EntrySource> sources;

  private final List<EntryPublisher> publishers;

  private final boolean useSystemPropertiesFallback;

  private final List<String> keyInclusions;

  /**
   * Constructor.
   */
  public AppContextRequest(final String id, final List<EntrySource> sources, final List<EntryPublisher> publishers) {
    this(id, null, sources, publishers, true, null);
  }

  /**
   * Constructor.
   */
  public AppContextRequest(final String id, final AppContext parent, final List<EntrySource> sources,
                           final List<EntryPublisher> publishers, final boolean useSystemPropertiesFallback,
                           final List<String> keyInclusions)
  {
    this.id = Preconditions.checkNotNull(id);
    this.parent = parent;
    this.sources = new ArrayList<EntrySource>(Preconditions.checkNotNull(sources));
    this.publishers = new ArrayList<EntryPublisher>(Preconditions.checkNotNull(publishers));
    this.useSystemPropertiesFallback = useSystemPropertiesFallback;
    this.keyInclusions = new ArrayList<String>();
    if (keyInclusions != null) {
      this.keyInclusions.addAll(keyInclusions);
    }
  }

  /**
   * Returns the AppContext ID, never {@code null}.
   *
   * @return the ID
   */
  public String getId() {
    return id;
  }

  /**
   * Returns a reference to parent {@link AppContext} if any.
   *
   * @return parent context or {@code null}.
   */
  public AppContext getParent() {
    return parent;
  }

  /**
   * Maintains the list of {@link EntrySource} to be used when creating {@link AppContext}. The order of the source
   * list is from "least important" to "most important" (ascending by importance). If you used some factory method
   * that returned some predefined source(s) already, you usually want to do something like this:
   *
   * <pre>
   * req.getSources().add( 0, new PropertiesFileEntrySource( new File( &quot;mostimportant.properties&quot; ) ) );
   * req.getSources().add( 0, new PropertiesFileEntrySource( new File( &quot;leastimportant.properties&quot; ) ) );
   * </pre>
   *
   * By using approach like this above, you always ensure that "least" important is the 1st (index 0) element of the
   * list, while the "bit more" important sources are moved forward (to list index greater than 0), while you do not
   * disturb the order of the list "tail", where other important sources are (doing the prefix and key inclusion is
   * handled). Still, if you know what you do, you can still invoke {@link List#clear()} and set the ordering you
   * want
   * from the scratch or just insert where you want without clearing the list. To rehearse, last {@link EntrySource}
   * "wins", meaning, if it contributes a key-value (or multiple of them) to the context, and a mapping for key(s)
   * exists, they will be overridden, stomped over by next source.
   *
   * @return list of ordered {@link EntrySource} sources to be used when {@link AppContext} is being created.
   */
  public List<EntrySource> getSources() {
    return sources;
  }

  /**
   * Maintains the list of {@link EntryPublisher} to be used when creating {@link AppContext}. If you don't want any
   * publishing to happen (default is simple "console" publishing, writing {@link AppContext} out to console
   * (System.out) or log if SLF4J is found on class path). You can safely to {@link List#clear()} if you don't need
   * any publishing.
   *
   * @return list of publishers to be used.
   */
  public List<EntryPublisher> getPublishers() {
    return publishers;
  }

  /**
   * A flag denoting will {@link System#getProperties()} be used in interpolation or not when creating
   * {@link AppContext}. If {@code true}, system properties source will be added as "least important" source, but it
   * will NOT get into {@link AppContext} map. It will be used in interpolation only. By default, this is {@code
   * true}
   * when request created over some {@link Factory} method.
   *
   * @return {@code true} if system properties should be consulted too when interpolating {@link AppContext}.
   */
  public boolean isUseSystemPropertiesFallback() {
    return useSystemPropertiesFallback;
  }

  /**
   * Returns the "key inclusions" for this request. If there are key inclusions, same sources will be created for
   * them
   * as for "prefixed" inclusions based on {@link AppContext} ID.
   *
   * @return list of keys to include.
   */
  public List<String> getKeyInclusions() {
    return keyInclusions;
  }
}
