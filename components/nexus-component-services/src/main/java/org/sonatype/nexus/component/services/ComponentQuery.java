package org.sonatype.nexus.component.services;

// Maybe this could be similar to RecordQuery, and defined in a way
// that keeps domain-specific objects out of it. E.g. a map of constrained
// attributes, keyed by string, valued by Object.
public class ComponentQuery
{
  private Integer limit;

  private Integer skip;

  private Object skipDomainObject; // provide a way to do RecordQuery's skipRecord technique under the hood?

  private String orderBy;

  private boolean descending;
}
