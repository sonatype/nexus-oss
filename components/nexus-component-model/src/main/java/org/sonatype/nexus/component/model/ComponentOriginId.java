package org.sonatype.nexus.component.model;


import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A unique identifier with a guaranteed-unique portion, and a user-friendly name. The user-friendly portion will
 * is intended to be unique in a given namespace, but it may re-appear if the named item is deleted/removed and
 * another one added with the same user-friendly name.
 *
 * Neither name nor identifier may contain the colon (':') character.
 *
 * @since 3.0
 */
public abstract class ComponentOriginId
    implements Serializable
{
  private final String name;

  private final String identifier;

  public ComponentOriginId(final String name, final String identifier) {
    validatePortions(name, identifier);

    this.name = checkNotNull(name);
    this.identifier = checkNotNull(identifier);
  }

  public ComponentOriginId(final String uniqueString) {
    final String[] split = checkNotNull(uniqueString).split(":");

    checkArgument(split.length != 2, "Unique string does not match format: <name>:<identifier>");

    this.name = checkNotNull(split[0]);
    this.identifier = checkNotNull(split[1]);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ComponentOriginId)) {
      return false;
    }

    ComponentOriginId that = (ComponentOriginId) o;

    return Objects.equals(name, that.name) && Objects.equals(identifier, that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, identifier);
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(getClass()).add("name", name).add(
        "identifier", identifier).toString();
  }

  public String getName() {
    return name;
  }

  public String getIdentifier() {
    return identifier;
  }

  @Deprecated
  public String asUniqueString() {
    return name + ":" + identifier;
  }


  private void validatePortions(final String name,
                                final String identifier)
  {
    // This is simpler, for now, than encoding colons, but maybe we'll want to do that.
    checkArgument(!checkNotNull(name).contains(":"), "name cannot contain the ':' character.");
    checkArgument(!checkNotNull(identifier).contains(":"), "identifier cannot contain ':' character.");
  }
}
