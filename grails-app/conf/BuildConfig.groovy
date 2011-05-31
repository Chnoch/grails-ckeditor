grails.project.class.dir = "target/classes"

grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"

grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsHome()

        mavenLocal()
        mavenCentral()
    }

    plugins {
        build (":release:1.0.0.M2") {
			export = false
		}
    }

    dependencies {
    }

}
