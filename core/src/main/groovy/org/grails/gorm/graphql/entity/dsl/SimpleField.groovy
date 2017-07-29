package org.grails.gorm.graphql.entity.dsl

import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.entity.dsl.helpers.Nullable
import org.grails.gorm.graphql.entity.dsl.helpers.Typed
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.types.GraphQLTypeManager

@CompileStatic
class SimpleField extends Field<SimpleField> implements Typed<SimpleField> {

	GraphQLOutputType getType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
		resolveOutputType(typeManager, mappingContext)
	}

	@Override
	GraphQLInputType getInputType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
		resolveInputType(typeManager, mappingContext, nullable)
	}

	void validate() {
		super.validate()

		if (returnType == null) {
            throw new IllegalArgumentException('A return type is required for creating fields')
        }
	}
}
