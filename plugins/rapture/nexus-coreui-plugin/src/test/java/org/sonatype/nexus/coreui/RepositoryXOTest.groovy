package org.sonatype.nexus.coreui

import javax.validation.Validation
import javax.validation.Validator

import org.sonatype.nexus.validation.group.Create

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class RepositoryXOTest
    extends Specification
{

  @Shared
  Validator validator = Validation.buildDefaultValidatorFactory().validator

  static List INVALID_NAMES = ['_', '#', ',', 'A', '*', ' ', '#', '\'', '\\', '/', '?', '<', '>', '|', ' ', '\r',
                               '\n', '\t', ',', '.'].asImmutable()

  static final List VALID_NAMES = ['foo123!@$%^&()-+=[];:~`', 'a_'].asImmutable()

  def "Name is always required"() {
    when:
      def validations = validator.validate(new RepositoryXO([attributes: [any: 'any']]))

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'name'
      validations[0].message == 'may not be empty'
  }

  def "Attributes are always required, and cannot be empty"(Map attributes) {
    when:
      Map properties = [
          name      : 'foo',
          attributes: attributes
      ]
      def validations = validator.validate(new RepositoryXO(properties))

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'attributes'
      validations[0].message == 'may not be empty'

    where:
      attributes << [null, [:]]
  }

  @Unroll
  def "The name: #name should not validate"(String name) {
    when:
      Map properties = [
          name      : name,
          attributes: [any: 'any']
      ]
      def validations = validator.validate(new RepositoryXO(properties))

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'name'

    where:
      name << INVALID_NAMES
  }

  @Unroll
  def "The name: #name should be valid"(String name) {
    when:
      Map properties = [
          name      : name,
          attributes: [any: 'any']
      ]
      def validations = validator.validate(new RepositoryXO(properties))

    then:
      validations.isEmpty()

    where:
      name << VALID_NAMES
  }

  def "The recipe field is only required on creation"() {
    setup:
      Map baseConfiguration = [
          name      : 'foo',
          attributes: [any: 'any']
      ]
    when:
      def validations = validator.validate(new RepositoryXO(baseConfiguration), Create)

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'recipe'

    when:
      baseConfiguration.recipe = 'any'
      validations = validator.validate(new RepositoryXO(baseConfiguration), Create)

    then:
      validations.isEmpty()
  }

}
