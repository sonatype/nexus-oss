package org.sonatype.nexus.security.filter;

import javax.inject.Provider;
import javax.servlet.Filter;

import com.google.inject.Key;
import com.google.inject.name.Names;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link Filter} providers.
 *
 * @since 2.8
 */
public class FilterProviderSupport
  implements Provider<Filter>
{
  private final Filter filter;

  public FilterProviderSupport(final Filter filter) {
    this.filter = checkNotNull(filter);
  }

  @Override
  public Filter get() {
    return filter;
  }

  public static Key<Filter> filterKey(final String name) {
    return Key.get(Filter.class, Names.named(name));
  }
}
