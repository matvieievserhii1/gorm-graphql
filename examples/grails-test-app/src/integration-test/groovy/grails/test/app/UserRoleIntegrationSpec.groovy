package grails.test.app

import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.spockframework.util.StringMessagePrintStream
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class UserRoleIntegrationSpec extends Specification implements GraphQLSpec {

    @Shared Long userId
    @Shared Long roleId

    @OnceBefore
    void createUserAndRole() {
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "admin@email.com",
                        firstName: "First",
                        lastName: "Last"
                    }
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                }) {
                    id
                }
            }
        """)
        JSONObject obj = resp.json.data.userCreate
        userId = obj.id

        resp = graphQL.graphql("""
            mutation {
                roleCreate(role: {
                    authority: "ROLE_ADMIN"
                }) {
                    id
                }
            }
        """)
        obj = resp.json.data.roleCreate
        roleId = obj.id
    }

    void "test creating an entity with a complex composite id"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userRoleCreate(userRole: {
                    user: {
                        id: ${userId}
                    },
                    role: {
                        id: ${roleId}
                    }
                }) {
                    user {
                        profile {
                            email
                        }
                    }
                    role {
                        authority
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.userRoleCreate

        then:
        obj.user.profile.email == 'admin@email.com'
        obj.role.authority == 'ROLE_ADMIN'
    }

    void "test reading an entity with a complex composite id"() {
        given:
        PrintStream originalOut = System.out
        String query
        int outCount = 0
        System.setOut(new StringMessagePrintStream() {
            @Override
            protected void printed(String message) {
                query = message
                outCount++
            }
        })

        when:
        def resp = graphQL.graphql("""
            {
                userRole(role: ${roleId}, user: ${userId}) {
                    user {
                        id
                    }
                    role {
                        id
                    }
                }
            }
        """.toString())
        JSONObject obj = resp.json.data.userRole

        then:
        obj.user.id == userId
        obj.role.id == roleId
        outCount == 1
        query == 'Hibernate: select this_.user_id as user_id1_12_0_, this_.role_id as role_id2_12_0_ from user_role this_ where this_.user_id=? and this_.role_id=? limit ?\n'

        when:
        outCount = 0
        resp = graphQL.graphql("""
            {
                userRole(role: ${roleId}, user: ${userId}) {
                    user {
                        profile {
                            email
                        }
                    }
                    role {
                        authority
                    }
                }
            }
        """.toString())
        obj = resp.json.data.userRole

        then: 'The user and role will be fetched with the same query'
        obj.user.profile.email == 'admin@email.com'
        obj.role.authority == 'ROLE_ADMIN'
        outCount == 1
        query == 'Hibernate: select this_.user_id as user_id1_12_2_, this_.role_id as role_id2_12_2_, user2_.id as id1_11_0_, user2_.version as version2_11_0_, user2_.manager_id as manager_3_11_0_, user2_.added_numbers as added_nu4_11_0_, user2_.address_zip as address_5_11_0_, user2_.address_city as address_6_11_0_, user2_.address_state as address_7_11_0_, user2_.profile_first_name as profile_8_11_0_, user2_.profile_last_name as profile_9_11_0_, user2_.profile_email as profile10_11_0_, role3_.id as id1_8_1_, role3_.version as version2_8_1_, role3_.authority as authorit3_8_1_ from user_role this_ inner join user user2_ on this_.user_id=user2_.id inner join role role3_ on this_.role_id=role3_.id where this_.user_id=? and this_.role_id=?\n'

        cleanup:
        System.setOut(originalOut)
    }

    void "test we cannot update a UserRole because it is turned off in the mapping"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userRoleUpdate(role: ${roleId}, user: ${userId}) {
                    user {
                        id
                    }
                    role {
                        id
                    }
                }
            }
        """.toString())
        JSONObject result = resp.json

        then:
        result.errors.size() == 1
        result.errors[0].message == "Validation error of returnType FieldUndefined: Field userRoleUpdate is undefined"
    }

    void "test listing entities with a complex composite id"() {
        when:
        def resp = graphQL.graphql("""
            {
                userRoleList {
                    user {
                        profile {
                            email
                        }
                    }
                    role {
                        authority
                    }
                }
            }
        """.toString())
        JSONArray obj = resp.json.data.userRoleList

        then:
        obj.size() == 1
        obj[0].user.profile.email == 'admin@email.com'
        obj[0].role.authority == 'ROLE_ADMIN'
    }

    void "test custom query operation added in the mapping"() {
        when:
        def resp = graphQL.graphql("""
            {
                usersByRole(role: ${roleId}) {
                    profile {
                        email
                    }
                }
            }
        """.toString())
        JSONArray obj = resp.json.data.usersByRole

        then:
        obj.size() == 1
        obj[0].profile.email == 'admin@email.com'
    }

    void "test custom mutation operation added in the mapping"() {
        setup: 'Add another role to the existing user'
        def resp = graphQL.graphql("""
            mutation {
                roleCreate(role: {
                    authority: "ROLE_USER"
                }) {
                    id
                }
            }
        """)
        Long newRoleId = resp.json.data.roleCreate.id
        graphQL.graphql("""
            mutation {
                userRoleCreate(userRole: {
                    user: {
                        id: ${userId}
                    },
                    role: {
                        id: ${newRoleId}
                    }
                }) {
                    user {
                        id
                    }
                    role {
                        id
                    }
                }
            }
        """.toString())

        when:
        resp = graphQL.graphql("""
            {
                userRoleList {
                    role {
                        authority
                    }
                }
            }
        """.toString())
        JSONArray list = resp.json.data.userRoleList

        then:
        list.size() == 2

        when:
        resp = graphQL.graphql("""
            mutation {
                revokeAllRoles(user: ${userId}) {
                    success
                }
            }
        """.toString())
        JSONObject obj = resp.json.data.revokeAllRoles

        then:
        obj.success

        when:
        resp = graphQL.graphql("""
            {
                userRoleList {
                    user {
                        id
                    }
                    role {
                        id
                    }
                }
            }
        """.toString())
        list = resp.json.data.userRoleList

        then: 'Check if the delete worked'
        list.empty

        cleanup: 'Re-create the user role so the next test can delete it'
        graphQL.graphql("""
            mutation {
                userRoleCreate(userRole: {
                    user: {
                        id: ${userId}
                    },
                    role: {
                        id: ${roleId}
                    }
                }) {
                    user {
                        id
                    }
                    role {
                        id
                    }
                }
            }
        """.toString())
    }

    void "test deleting an entity with a complex composite id"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userRoleDelete(role: ${roleId}, user: ${userId}) {
                    success
                }
            }
        """.toString())
        JSONObject obj = resp.json.data.userRoleDelete

        then:
        obj.success
    }
}
