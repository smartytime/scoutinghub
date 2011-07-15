package scoutinghub

import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import org.springframework.security.access.AccessDeniedException
import grails.converters.JSON

@Secured(["ROLE_LEADER"])
class LeaderController {

    SpringSecurityService springSecurityService

    LeaderService leaderService

    TrainingService trainingService;

    def create = {
        int scoutGroupId = Integer.parseInt(params['scoutGroup.id'] ?: "0");
        return [scoutGroup: ScoutGroup.get(scoutGroupId)]

    }

    def getLeaderDetails = {
        Leader leader = Leader.get(params.id)
        def rtn = [id:leader.id,
                firstName: leader?.firstName,
                lastName:leader?.lastName,
                email:leader?.email]
        render rtn as JSON
    }

    def recheckLeaderMatch = {
        final Set<Leader> leaders = leaderService.findLeaders(params.scoutid, params.email, params.firstName, params.lastName, null);
        def rtn = [check: leaders?.find {it.id == Integer.parseInt(params.id)} != null]
        render rtn as JSON
    }


    def findLeaderMatch = {
        final Set<Leader> leaders = leaderService.findLeaders(params.scoutid, params.email, params.firstName, params.lastName, null);
        if (leaders?.size() > 0) {
            return [leaders:leaders]
        } else {
            render("")
        }
    }

    def index = {
        forward(action: 'profile')
    }

    def show = {
        forward(action: "view")
    }

    def profile = {
        forward(action: "view")
    }

    def merge = {
        Leader leaderA = Leader.get(Integer.parseInt(params.leaderA))
        Leader leaderB = Leader.get(Integer.parseInt(params.leaderB))

        return [leaderA: leaderA, leaderB: leaderB]
    }

    def saveProfile = {

        Leader leader = Leader.get(params.id);
        if (!leader.canBeAdministeredBy(springSecurityService.currentUser)) {
            throw new AccessDeniedException("Can't edit this user");

        }
        leader.firstName = params.firstName
        leader.lastName = params.lastName
        leader.email = params.email
        leader.phone = params.phone

        if(!leader.save()) {
            flash.leaderError = leader
            flash.error = true
            redirect(action: "view", id:leader.id, params:[edit:true])
        } else {
            leader.reindex()
            redirect(action: "view", id: leader.id)
        }

    }

    def doMerge = {
        Leader leaderA = Leader.get(Integer.parseInt(params.leaderA))
        Leader leaderB = Leader.get(Integer.parseInt(params.leaderB))

        leaderService.mergeLeaders(leaderA,leaderB);
        trainingService.recalculatePctTrained(leaderA);
        redirect(view:"view", id:leaderA.id)

    }

    def accountCreated = {
        Leader leader = springSecurityService.currentUser
        leader.reindex()
        forward(action:'view')
    }

    def view = {
        Date now = new Date()
        Leader leader
        if (params.id) {
            leader = Leader.get(params.id)
            Leader loggedIn = springSecurityService.currentUser
            if (!leader.canBeAdministeredBy(loggedIn) && !loggedIn.hasRole("ROLE_ADMIN")) {
                redirect(controller: "login", action: "denied")
                return
            }


        } else {
            leader = springSecurityService.currentUser
        }

        if(!leader) {
            redirect(controller: "login", action: "denied")
            return
        }
        def requiredCertifications
        def certificationInfo = []
        if (leader?.groups?.size() > 0) {

            def c = ProgramCertification.createCriteria()

            requiredCertifications = c.list {
                and {
                    or {
                        inList('unitType', leader.groups?.collect {it.scoutGroup.unitType})
                        inList('positionType', leader.groups?.collect {it.leaderPosition})
                    }
                    eq('required', true)
                }
                eq('required', true)
                lt('startDate', now)
                gt('endDate', now)

                certification {
                    sort: 'name'
                }
            }

            def certificationIds = new HashSet();

            requiredCertifications?.each {
                ProgramCertification programCertification ->
                if (!certificationIds.contains(programCertification.certification.id)) {
                    certificationInfo << new LeaderCertificationInfo(leader, programCertification.certification)
                    certificationIds.add(programCertification.certification.id)
                }
            }


        }

        def rtn = [certificationInfo: certificationInfo, leader: leader]

        return rtn


    }

    def training = {}
}
