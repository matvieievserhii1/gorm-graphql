package org.grails.gorm.graphql.types.input

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager

/**
 * The class used to define which properties are available
 * when updating an entity
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@InheritConstructors
class UpdateInputObjectTypeBuilder extends InputObjectTypeBuilder {

    GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
            .excludeTimestamps()
            .excludeIdentifiers()
            .alwaysNullable()

    GraphQLPropertyType type = GraphQLPropertyType.UPDATE
}
