package grails32.test.app

class Book {

    String title

    static belongsTo = [author: Author]

    static constraints = {
    }
}
