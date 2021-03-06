import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.facebook.FacebookHelper
import org.springframework.security.facebook.MappedFacebookAuthenticationProvider
import java.util.concurrent.Executors
import scoutinghub.ScoutingHubAuthenticationSuccessHandler
import grails.plugins.springsecurity.SpringSecurityService
import scoutinghub.infusionsoft.InfusionsoftLeaderRegisteredEventHandler
import scoutinghub.infusionsoft.LeaderInvitedEventHandler

import java.util.concurrent.Executors

import scoutinghub.*

// Place your Spring DSL code here
beans = {

    customDatePropertyRegistrar(CustomDatePropertyRegistrar)

    def conf = SpringSecurityUtils.securityConfig
    if (!conf || !conf.active) {
        return
    }

    scoutingHubAuthenticationSuccessHandler(ScoutingHubAuthenticationSuccessHandler) {
        springSecurityService = ref("springSecurityService")
    }

    infusionsoftLeaderInvitedEventHandler(LeaderInvitedEventHandler)

    SpringSecurityUtils.loadSecondaryConfig 'DefaultOpenIdSecurityConfig'
    // have to get again after overlaying DefaultOpenIdSecurityConfig
    conf = SpringSecurityUtils.securityConfig

    if (!conf.openid.active) {
        return
    }

    //SpringSecurityUtils.registerProvider 'facebookAuthenticationProvider'

    userDetailsService(ScoutUserDetailsService) {
        grailsApplication = ref("grailsApplication")
    }

    facebookAuthenticationFilter(FacebookSocialAuthenticationFilter) {
        authenticationManager = ref("authenticationManager")
        authenticationSuccessHandler = ref("authenticationSuccessHandler")
        authenticationFailureHandler = ref("facebookAuthenticationFailureHandler")
        socialFacebookHelper = ref("facebookHelper")
        facebookHelper = ref("facebookHelper")
        springSecurityService = ref("springSecurityService")
        grailsApplication = ref("grailsApplication")

    }

    facebookAuthenticationFailureHandler(FacebookAuthenticationFailureHandler) {
//        redirectStrategy = ref('redirectStrategy')
        defaultFailureUrl = conf.failureHandler.defaultFailureUrl //'/login/authfail?login_error=1'
        useForward = conf.failureHandler.useForward // false
        ajaxAuthenticationFailureUrl = conf.failureHandler.ajaxAuthFailUrl // '/login/authfail?ajax=true'
        exceptionMappings = conf.failureHandler.exceptionMappings // [:]
    }


    facebookAuthenticationProvider(MappedFacebookAuthenticationProvider) {
        userDetailsService = ref("userDetailsService")
    }

    facebookHelper(FacebookHelper) {
        apiKey = "7ff080f0a28d435c77b2506472e4add1"
        secret = "139698635a23301fa00e43cbdd254222"
    }

    openIDAuthenticationFilter(OpenIdSocialAuthenticationFilter) {
        claimedIdentityFieldName = conf.openid.claimedIdentityFieldName // openid_identifier
        consumer = ref('openIDConsumer')
        rememberMeServices = ref('rememberMeServices')
        authenticationManager = ref('authenticationManager')
        authenticationSuccessHandler = ref('authenticationSuccessHandler')
        authenticationFailureHandler = ref('authenticationFailureHandler')
        authenticationDetailsSource = ref('authenticationDetailsSource')
        sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
        springSecurityService = ref("springSecurityService")
        filterProcessesUrl = '/j_spring_openid_security_check' // not configurable
    }

    executorService(grails.plugin.executor.SessionBoundExecutorService) { bean->
        bean.destroyMethod = 'destroy' //keep this destroy method so it can try and clean up nicely
        sessionFactory = ref("sessionFactory")
        //I want only a single thread - because I don't want indexing jobs running at the same time.
        executor = Executors.newFixedThreadPool(1)
    }

}
