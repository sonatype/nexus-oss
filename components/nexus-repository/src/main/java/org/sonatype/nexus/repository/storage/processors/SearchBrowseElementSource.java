package org.sonatype.nexus.repository.storage.processors;

import org.sonatype.nexus.repository.search.SearchService;
import org.sonatype.nexus.repository.storage.ProcessorContext;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Element source that uses {@link SearchService#browse(QueryBuilder)} method and {@link SearchHit} as elements.
 *
 * @since 3.0
 */
public class SearchBrowseElementSource
    extends ValueElementSource<SearchHit>
{
  private final SearchService searchService;

  private final QueryBuilder queryBuilder;

  public SearchBrowseElementSource(final SearchService searchService,
                                   final QueryBuilder queryBuilder)
  {
    super(SearchHit.class);
    this.searchService = checkNotNull(searchService);
    this.queryBuilder = checkNotNull(queryBuilder);
  }

  @Override
  public Iterable<SearchHit> elements(final ProcessorContext context) {
    return searchService.browse(queryBuilder);
  }
}
