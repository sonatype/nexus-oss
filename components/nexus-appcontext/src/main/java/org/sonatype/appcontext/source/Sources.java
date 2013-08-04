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

package org.sonatype.appcontext.source;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.appcontext.source.filter.FilteredEntrySource;
import org.sonatype.appcontext.source.filter.KeyEqualityEntryFilter;
import org.sonatype.appcontext.source.filter.KeyPrefixEntryFilter;
import org.sonatype.appcontext.source.keys.KeyTransformer;
import org.sonatype.appcontext.source.keys.KeyTransformingEntrySource;
import org.sonatype.appcontext.source.keys.LegacySystemEnvironmentKeyTransformer;
import org.sonatype.appcontext.source.keys.NoopKeyTransformer;
import org.sonatype.appcontext.source.keys.PrefixRemovingKeyTransformer;

/**
 * Helper to create various EntrySources.
 *
 * @author cstamas
 */
public final class Sources
{
  private Sources() {
  }

  /**
   * Creates a properly ordered list of EntrySources with variations to be used as "default" entry sources. Order is
   * system env then system properties. It also takes care of "aliases", ordering the before "id" to make "id"
   * override aliases if needed.
   */
  public static List<EntrySource> getDefaultSources(final String id, final List<String> aliases) {
    final ArrayList<EntrySource> result = new ArrayList<EntrySource>(2);

    for (String alias : aliases) {
      result.add(getPrefixTargetedEntrySource(new SystemEnvironmentEntrySource(),
          new LegacySystemEnvironmentKeyTransformer('-'), alias + "-"));
    }

    result.add(getPrefixTargetedEntrySource(new SystemEnvironmentEntrySource(),
        new LegacySystemEnvironmentKeyTransformer('-'), id + "-"));

    for (String alias : aliases) {
      result.add(getPrefixTargetedEntrySource(new SystemPropertiesEntrySource(), alias + "."));
    }

    result.add(getPrefixTargetedEntrySource(new SystemPropertiesEntrySource(), id + "."));

    return result;
  }

  /**
   * Creates a properly ordered list of EntrySources with variations to be used for prefixed entry sources. Order is
   * system env then system properties.
   */
  public static List<EntrySource> getDefaultPrefixTargetedSources(final String prefix) {
    final ArrayList<EntrySource> result = new ArrayList<EntrySource>(2);
    result.add(getPrefixTargetedEntrySource(new SystemEnvironmentEntrySource(),
        new LegacySystemEnvironmentKeyTransformer('-'), prefix + "-"));
    result.add(getPrefixTargetedEntrySource(new SystemPropertiesEntrySource(), prefix + "."));
    return result;
  }

  /**
   * Creates a properly ordered list of EntrySources with variations to be used for selected (listed keys, tested on
   * equality) entry sources. Order is system env then system properties.
   */
  public static List<EntrySource> getDefaultSelectTargetedSources(final String... keys) {
    final ArrayList<EntrySource> result = new ArrayList<EntrySource>(2);
    result.add(getSelectTargetedEntrySource(new SystemEnvironmentEntrySource(),
        new LegacySystemEnvironmentKeyTransformer('-'), keys));
    result.add(getSelectTargetedEntrySource(new SystemPropertiesEntrySource(), keys));
    return result;
  }

  /**
   * Gets a targeted entry source (see {@link #getPrefixTargetedEntrySource(EntrySource, KeyTransformer, String)} for
   * complete description) that uses {@link NoopKeyTransformer}.
   */
  public static EntrySource getPrefixTargetedEntrySource(final EntrySource source, final String prefix) {
    return getPrefixTargetedEntrySource(source, new NoopKeyTransformer(), prefix);
  }

  /**
   * Gets a targeted entry source (see {@link #getSelectTargetedEntrySource(EntrySource, KeyTransformer, String)} for
   * complete description) that uses {@link NoopKeyTransformer}.
   */
  public static EntrySource getSelectTargetedEntrySource(final EntrySource source, final String... keys) {
    return getSelectTargetedEntrySource(source, new NoopKeyTransformer(), keys);
  }

  /**
   * Returns a "targeted entry source" that does:
   * <ul>
   * <li>performs key transformation with supplied key transformer</li>
   * <li>filters keys with supplied prefix</li>
   * <li>performs a key transformation with prefixRemoving transformer</li>
   * </ul>
   * Hence, if you have an entry source having keys "myapp.foo" and "bar", and you pass in prefix "myapp." (not the
   * ending dot!), the resulting entry sources will deliver only one key, the "foo" (filtering discareded "bar", it
   * does not have prefix "myapp.", and prefixRemoving key transformation stripped off "myapp." prefix). This is a
   * special case of FilteredEntrySource that "cherry-picks" based on prefix (so far does same as FilteredEntrySource
   * with KeyPrefixEntryFilter), but also removes the matched prefix from the matched keys before putting it into
   * result.
   */
  public static EntrySource getPrefixTargetedEntrySource(final EntrySource source, final KeyTransformer transformer,
                                                         final String prefix)
  {
    final KeyTransformingEntrySource transformingEntrySource = new KeyTransformingEntrySource(source, transformer);

    final FilteredEntrySource filteredEntrySource =
        new FilteredEntrySource(transformingEntrySource, new KeyPrefixEntryFilter(prefix));

    return new KeyTransformingEntrySource(filteredEntrySource, new PrefixRemovingKeyTransformer(prefix));
  }

  /**
   * Returns a "targeted entry source" that does:
   * <ul>
   * <li>performs key transformation with supplied key transformer</li>
   * <li>filters (cherry-picks) keys with supplied keys (equality)</li>
   * <li>does not performs a key transformation</li>
   * </ul>
   * Hence, if you have an entry source having keys "foo" and "bar", and you pass in keys [foo], the resulting entry
   * sources will deliver only one key, the "foo" (filtering discareded "bar"). This is a special case of
   * FilteredEntrySource that "cherry-picks" based on keys (so far does same as FilteredEntrySource with
   * KeyEqualityEntryFilter).
   */
  public static EntrySource getSelectTargetedEntrySource(final EntrySource source, final KeyTransformer transformer,
                                                         final String... keys)
  {
    if (transformer != null) {
      final KeyTransformingEntrySource transformingEntrySource =
          new KeyTransformingEntrySource(source, transformer);

      return new FilteredEntrySource(transformingEntrySource, new KeyEqualityEntryFilter(keys));
    }
    else {
      return new FilteredEntrySource(source, new KeyEqualityEntryFilter(keys));
    }
  }
}
