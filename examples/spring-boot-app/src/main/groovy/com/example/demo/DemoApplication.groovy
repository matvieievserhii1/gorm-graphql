package com.example.demo

import graphql.GraphQL
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.Schema
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@EnableAutoConfiguration
@ComponentScan
@CompileStatic
@SpringBootApplication
class DemoApplication {

	static void main(String[] args) {
		SpringApplication.run DemoApplication, args
	}

	@Bean
	GraphQL graphQL(@Autowired HibernateDatastore datastore) {
		new GraphQL(new Schema(datastore.mappingContext).generate())
	}
}
