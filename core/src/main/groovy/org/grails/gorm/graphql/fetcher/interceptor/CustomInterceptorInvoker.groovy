package org.grails.gorm.graphql.fetcher.interceptor

import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.interceptor.GraphQLFetcherInterceptor

/**
 * Executes interceptors for custom operations
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@Slf4j
abstract class CustomInterceptorInvoker extends InterceptorInvoker {

    @Override
    final boolean invoke(GraphQLFetcherInterceptor interceptor, DataFetchingEnvironment environment, GraphQLDataFetcherType type) {
        final String NAME = getName(environment)
        boolean result = invoke(interceptor, NAME, environment)
        if (!result) {
            log.info("Execution of ${NAME} was prevented by an interceptor")
        }
        result
    }

    abstract boolean invoke(GraphQLFetcherInterceptor interceptor, String name, DataFetchingEnvironment environment)
}
