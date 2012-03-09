/**
 *
 */
package de.is24.nexus.yum.alias.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


/**
 * @author BVoss
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AliasMapping {
  private final String repoId;

  private final String alias;

  private final String version;

  @SuppressWarnings("unused") // JaxB constructor, don't remove it!
  private AliasMapping() {
    this(null, null, null);
  }

  public AliasMapping(final String repoId, final String alias,
    final String version) {
    this.alias = alias;
    this.repoId = repoId;
    this.version = version;
  }

  public String getRepoId() {
    return repoId;
  }

  public String getAlias() {
    return alias;
  }

  public String getVersion() {
    return version;
  }

  public AliasKey getAliasKey() {
    return new AliasKey(repoId, alias);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((alias == null) ? 0 : alias.hashCode());
    result = (prime * result) + ((repoId == null) ? 0 : repoId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    AliasMapping other = (AliasMapping) obj;
    if (alias == null) {
      if (other.alias != null) {
        return false;
      }
    } else if (!alias.equals(other.alias)) {
      return false;
    }
    if (repoId == null) {
      if (other.repoId != null) {
        return false;
      }
    } else if (!repoId.equals(other.repoId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AliasMapping [repoId=" + repoId + ", alias=" + alias +
      ", version=" + version + "]";
  }


}
