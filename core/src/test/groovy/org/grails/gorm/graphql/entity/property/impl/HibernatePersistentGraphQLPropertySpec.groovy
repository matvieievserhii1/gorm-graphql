package org.grails.gorm.graphql.entity.property.impl

import grails.gorm.annotation.Entity
import graphql.Scalars
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import org.codehaus.groovy.util.HashCodeHelper
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.gorm.graphql.HibernateSpec
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static org.grails.gorm.graphql.types.GraphQLPropertyType.*

class HibernatePersistentGraphQLPropertySpec extends HibernateSpec {

    GraphQLTypeManager typeManager

    List<Class> getDomainClasses() { [Book, Book2, Author, Tag, Metadata, OtherMetadata] }

    void setup() {
        typeManager = Mock(GraphQLTypeManager)
    }

    PersistentGraphQLProperty getProperty(Class clazz, String name) {
        PersistentProperty p = mappingContext.getPersistentEntity(clazz.name).getPropertyByName(name)
        new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
    }

    GraphQLObjectType dummyObjectType() {
        GraphQLObjectType.newObject().name('x').build()
    }

    GraphQLInputObjectType dummyInputObjectType() {
        GraphQLInputObjectType.newInputObject().name('x').build()
    }

    void "test constructor with simple property (non null)"() {
        PersistentGraphQLProperty property = getProperty(Book, 'title')

        expect:
        property.name == 'title'
        property.type == String
        !property.collection
        !property.nullable
    }

    void "test constructor with simple property (null)"() {
        PersistentGraphQLProperty property = getProperty(Book, 'description')

        expect:
        property.name == 'description'
        property.type == String
        !property.collection
        property.nullable
    }

    void "test constructor with id property"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).identity
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'id'
        property.type == Long
        !property.collection
        !property.nullable
    }

    void "test constructor with composite id property"() {
        PersistentGraphQLProperty property = getProperty(Book2, 'title')

        expect:
        property.name == 'title'
        property.type == String
        !property.collection
        !property.nullable
    }

    void "test constructor with an enum"() {
        PersistentGraphQLProperty property = getProperty(Book, 'bookType')

        expect:
        property.name == 'bookType'
        property.type == BookType
        !property.collection
        !property.nullable
    }

    void "test constructor with a collection of enums"() {
        PersistentGraphQLProperty property = getProperty(Book, 'otherBookTypes')

        expect:
        property.name == 'otherBookTypes'
        property.type == BookType
        property.collection
        !property.nullable
    }

    void "test constructor with a toMany"() {
        PersistentGraphQLProperty property = getProperty(Book, 'authors')

        expect:
        property.name == 'authors'
        property.type == Author
        property.collection
        !property.nullable
    }

    void "test constructor with a simple toMany"() {
        PersistentGraphQLProperty property = getProperty(Book, 'basics')

        expect:
        property.name == 'basics'
        property.type == String
        property.collection
        !property.nullable
    }

    void "test constructor with a toOne"() {
        PersistentGraphQLProperty property = getProperty(Book, 'metadata')

        expect:
        property.name == 'metadata'
        property.type == Metadata
        !property.collection
        !property.nullable
    }

    void "test graphQL type with simple property (non null)"() {
        PersistentGraphQLProperty property = getProperty(Book, 'title')

        when:
        property.getGraphQLType(typeManager, null)

        then:
        1 * typeManager.getType(String, false)
    }

    void "test graphQL type with simple property (null)"() {
        PersistentGraphQLProperty property = getProperty(Book, 'description')

        when:
        property.getGraphQLType(typeManager, null)

        then:
        1 * typeManager.getType(String, true)
    }

    void "test graphQL type with an enum (non null)"() {
        PersistentGraphQLProperty property = getProperty(Book, 'bookType')

        when:
        property.getGraphQLType(typeManager, null)

        then:
        1 * typeManager.getEnumType(BookType, false)
    }

    void "test graphQL type with an enum (null)"() {
        PersistentGraphQLProperty property = getProperty(Book, 'nullBookType')

        when:
        property.getGraphQLType(typeManager, null)

        then:
        1 * typeManager.getEnumType(BookType, true)
    }

    void "test graphQL type with a toMany that is mapped with graphql"() {
        PersistentGraphQLProperty property = getProperty(Book, 'authors')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Author.name), OUTPUT) >> dummyObjectType()
    }

    void "test graphQL type with a toMany that is mapped with graphql CREATE"() {
        PersistentGraphQLProperty property = getProperty(Book, 'authors')

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getMutationType(mappingContext.getPersistentEntity(Author.name), CREATE_NESTED, false) >> dummyInputObjectType()
    }

    void "test graphQL type with a toMany that is NOT mapped with graphql"() {
        PersistentGraphQLProperty property = getProperty(Book, 'tags')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Tag.name), OUTPUT) >> dummyObjectType()
    }

    void "test graphQL type with a toMany that is NOT mapped with graphql UPDATE"() {
        PersistentGraphQLProperty property = getProperty(Book, 'tags')

        when:
        property.getGraphQLType(typeManager, UPDATE)

        then:
        1 * typeManager.getMutationType(mappingContext.getPersistentEntity(Tag.name), UPDATE_NESTED, false) >> dummyInputObjectType()
    }

    void "test graphQL type with a simple toMany"() {
        PersistentGraphQLProperty property = getProperty(Book, 'basics')

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getType(String, false) >> Scalars.GraphQLString
    }

    void "test graphQL query type with a toOne that is NOT mapped with graphql"() {
        PersistentGraphQLProperty property = getProperty(Book, 'metadata')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Metadata.name), OUTPUT)
    }

    void "test graphQL query type with a toOne that is mapped with graphql"() {
        PersistentGraphQLProperty property = getProperty(Book, 'otherMetadata')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(OtherMetadata.name), OUTPUT)
    }

    void "test graphQL mutation type with a toOne that is mapped with graphql"() {
        PersistentGraphQLProperty property = getProperty(Book, 'otherMetadata')

        when:
        property.getGraphQLType(typeManager, propertyType)

        then:
        1 * typeManager.getMutationType(mappingContext.getPersistentEntity(OtherMetadata.name), queriedType, false)

        where:
        propertyType | queriedType
        CREATE       | CREATE_NESTED
        UPDATE       | UPDATE_NESTED
    }

    void "test graphQL type with a toOne that is NOT mapped with graphql"() {
        PersistentGraphQLProperty property = getProperty(Book, 'metadata')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Metadata.name), OUTPUT)

        when:
        property.getGraphQLType(typeManager, OUTPUT_EMBEDDED)

        then: 'The returnType gets set to OUTPUT since the property is not embedded'
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Metadata.name), OUTPUT)
    }

    void "test graphQL type with an embedded property that is a domain OUTPUT"() {
        PersistentGraphQLProperty property = getProperty(Book, 'otherMetadata2')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(OtherMetadata.name), OUTPUT_EMBEDDED)
    }

    void "test graphQL type with an embedded property that is NOT a domain OUTPUT"() {
        PersistentGraphQLProperty property = getProperty(Book, 'someOtherMetadata')

        when:
        property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(_ as PersistentEntity, OUTPUT_EMBEDDED)
    }

    void "test graphQL type with an embedded property that is a domain OUTPUT_EMBEDDED"() {
        PersistentGraphQLProperty property = getProperty(Book, 'otherMetadata2')

        when:
        property.getGraphQLType(typeManager, OUTPUT_EMBEDDED)

        then: 'The returnType stays the same since the property is embedded'
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(OtherMetadata.name), OUTPUT_EMBEDDED)
    }

    void "test graphQL type with an embedded property that is NOT a domain OUTPUT_EMBEDDED"() {
        PersistentGraphQLProperty property = getProperty(Book, 'someOtherMetadata')

        when:
        GraphQLType type = property.getGraphQLType(typeManager, OUTPUT_EMBEDDED)

        then: 'The returnType stays the same since the property is embedded'
        1 * typeManager.getQueryType(_ as PersistentEntity, OUTPUT_EMBEDDED) >> GraphQLObjectType.newObject().name('x').build()
        !(type instanceof GraphQLList)
    }
}

@Entity
class Book {
    Long id
    String title
    String description
    Metadata metadata
    BookType bookType
    BookType nullBookType
    
    OtherMetadata otherMetadata
    OtherMetadata otherMetadata2
    SomeOtherMetadata someOtherMetadata

    static hasMany = [authors: Author, tags: Tag, basics: String, otherBookTypes: BookType]

    static constraints = {
        description nullable: true
        nullBookType nullable: true
    }

    static embedded = ['otherMetadata2', 'someOtherMetadata']
}

@Entity
class Book2 implements Serializable {
    String title
    String description
    Metadata metadata
    BookType bookType

    static hasMany = [authors: Author, tags: Tag, basics: String]

    static mapping = {
        id composite: ['title', 'description']
    }

    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (title) {
            hashCode = HashCodeHelper.updateHash(hashCode, title)
        }
        if (description) {
            hashCode = HashCodeHelper.updateHash(hashCode, description)
        }
        hashCode
    }

    @Override
    boolean equals(Object other) {
        if (other instanceof Book2) {
            other.title == title && other.description == description
        }
    }

    static graphql = true
}

@Entity
class Author {
    String name
    static belongsTo = [book: Book]

    static graphql = true
}

@Entity
class Tag {
    String name
}

@Entity
class Metadata {
    String x
    String y
}

@Entity
class OtherMetadata {
    String x
    String y

    static graphql = true
}

class SomeOtherMetadata {
    String foo
}

enum BookType {
    OLD, NEW
}