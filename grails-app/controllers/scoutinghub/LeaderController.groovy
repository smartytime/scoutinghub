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
        return [addCommand: new AddLeaderToGroupCommand(unit: ScoutGroup.get(scoutGroupId))]
    }

    def save = {AddLeaderToGroupCommand addCommand ->

        if (!addCommand.validate()) {
            render(view: 'create', model: [errors: "There are errors", addCommand: addCommand])
        } else {
            Leader leader
            if (addCommand.foundLeader) {
                leader = addCommand.foundLeader
            } else {
                leader = new Leader()
                leader.firstName = addCommand.firstName
                leader.lastName = addCommand.lastName
                leader.email = addCommand.email
            }

            //If the scoutid doesn't exist, add it
            if (addCommand.scoutid) {
                if (!MyScoutingId.findByMyScoutingIdentifier(addCommand.scoutid)) {
                    leader.addToMyScoutingIds([myScoutingIdentifier: addCommand.scoutid])
                }
            }

            //If the leader is not already in the group, add it
            if (!leader.groups?.find {it.scoutGroup.id == addCommand.unit.id}) {
                leader.addToGroups([scoutGroup: addCommand.unit, leaderPosition: addCommand.unitPosition])
            }

            leader.save(failOnError: true)
            leader.reindex();
            trainingService.recalculatePctTrained(leader)
            redirect(controller: "scoutGroup", action: "show", id: addCommand.unit.id)
        }
    }

    def getLeaderDetails = {
        Leader leader = Leader.get(params.id)
        def rtn = [id: leader.id,
                firstName: leader?.firstName,
                lastName: leader?.lastName,
                email: leader?.email]
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
            return [leaders: leaders]
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

        if (!leader.save()) {
            flash.leaderError = leader
            flash.error = true
            redirect(action: "view", id: leader.id, params: [edit: true])
        } else {
            leader.reindex()
            redirect(action: "view", id: leader.id)
        }

    }

    def doMerge = {
        Leader leaderA = Leader.get(Integer.parseInt(params.leaderA))
        Leader leaderB = Leader.get(Integer.parseInt(params.leaderB))

        leaderService.mergeLeaders(leaderA, leaderB);
        trainingService.recalculatePctTrained(leaderA);
        redirect(view: "view", id: leaderA.id)

    }

    def accountCreated = {
        Leader leader = springSecurityService.currentUser
        leader.reindex()
        forward(action: 'view')
    }

    def ignoreDuplicate = {
        int leaderA = Integer.parseInt(params.leaderA)
        int leaderB = Integer.parseInt(params.leaderB)
        if (leaderA > 0 && leaderB > 0) {
            IgnoredDuplicate ignoredDuplicate = new IgnoredDuplicate()
            ignoredDuplicate.leaderId = leaderA
            ignoredDuplicate.suspectedLeaderId = leaderB
            ignoredDuplicate.save(failOnError:true)
            def rtn = [success:true]
            render rtn as JSON
        }
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

        //Let's query for potential duplicate records
        def duplicates = leaderService.findDuplicateLeaders(leader)
        List<IgnoredDuplicate> allIgnored = IgnoredDuplicate.findAllByLeaderId(leader.id)
        allIgnored?.each {IgnoredDuplicate ignoredDuplicate->
            duplicates.removeAll {it.id == ignoredDuplicate.suspectedLeaderId}
        }

        if (!leader) {
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

        //We need to build a list of "extra" certifications.  This would include any leader certification
        //not found in the query above.
        def extraCertificationInfo = []

        //Load up all certifications
        leader.certifications?.each { extraCertificationInfo << new LeaderCertificationInfo(it) }

        //Remove the ones already represented by required training
        certificationInfo.each { extraCertificationInfo.remove(it) }


        def rtn = [duplicates: duplicates, extraCertificationInfo:extraCertificationInfo, certificationInfo: certificationInfo, leader: leader]

        return rtn


    }

    def training = {}
}
