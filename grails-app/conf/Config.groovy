import grails.plugins.springsecurity.SecurityConfigType

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header=true
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "https://secure.scoutinghub.com/${appName}"
        google.key = "ABQIAAAAgoRonsXmaX5cVtBJUQdV_xRQbRNcLbW9ou7XtPmvSIlWQpGzBBSmS9E1da3JBVfLiWruMVA0KRqLgQ"
    }

    development {
        google.key = "ABQIAAAAgoRonsXmaX5cVtBJUQdV_xRQbRNcLbW9ou7XtPmvSIlWQpGzBBSmS9E1da3JBVfLiWruMVA0KRqLgQ"
    }
}

environments {
    production {
        scoutinghub.serverURL = "https://secure.scoutinghub.com/${appName}"
    }

    development {
        scoutinghub.serverURL = "http://dev.scoutinghub.com:8080/${appName}"
    }

    test {
        scoutinghub.serverURL = "http://localhost:8080/${appName}"
    }

}


// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
    info   'scoutinghub'

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.useSecurityEventListener = true
grails.plugins.springsecurity.userLookup.userDomainClassName = 'scoutinghub.Leader'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'scoutinghub.LeaderRole'
grails.plugins.springsecurity.authority.className = 'scoutinghub.Role'
grails.plugins.springsecurity.requestMap.className = 'scoutinghub.RequestMap'
grails.plugins.springsecurity.roleHierarchy = '''
   ROLE_ADMIN > ROLE_LEADER
   ROLE_ADMIN > ROLE_ANONYMOUS
   ROLE_LEADER > ROLE_ANONYMOUS
'''

grails.plugins.springsecurity.providerNames = ['openIDAuthProvider', 'facebookAuthenticationProvider','daoAuthenticationProvider', 'anonymousAuthenticationProvider', 'rememberMeAuthenticationProvider']
grails.plugins.springsecurity.securityConfigType = SecurityConfigType.Annotation
grails.plugins.springsecurity.openid.domainClass = 'scoutinghub.OpenID'
grails.plugins.springsecurity.openid.registration.autocreate=true

grails.plugins.springsecurity.rememberMe.persistent = true
grails.plugins.springsecurity.rememberMe.persistentToken.domainClassName = 'scoutinghub.PersistentToken'

elasticSearch.bulkIndexOnStartup = true

environments {
    development {
//        elasticSearch.client.mode = 'transport'
    }
}
// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
