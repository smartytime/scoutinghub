package scoutinghub

import org.compass.core.engine.SearchEngineQueryParseException
import grails.plugins.springsecurity.Secured
import grails.converters.JSON

@Secured(["ROLE_LEADER"])
class PermissionsController {

    def searchableService
    def springSecurityService
    ScoutGroupService scoutGroupService


    def index = {
    }

    def rebuild = {
        scoutGroupService.reindex()

        searchableService.index()
        render("Done")
    }

    def leaderQuery = {
        if (!params.leaderQuery?.trim()) {
            return [:]
        }
        try {
            Leader leader = springSecurityService.currentUser
            def results;
            List<String> allGroups = leader?.groups?.findAll {it.admin}?.collect {String.valueOf(it.scoutGroup.id)}
            if (allGroups?.size() > 0 || leader.hasRole("ROLE_ADMIN")) {
                results = Leader.search(params.leaderQuery?.trim() + "*", params)
            }
//                results = Leader.search(params.leaderQuery?.trim() + "*", params, filter: ScoutGroupFilter.createFilter(allGroups));

            return [results: results.results]
        } catch (SearchEngineQueryParseException ex) {
            return [parseException: true]
        }
    }

    def setPermission = {
        //Verify permission
        boolean checked = Boolean.parseBoolean(params.checked)
        Leader leader = Leader.get(params.leaderId)
        Role role = Role.get(params.roleId)

        if (checked) {
            LeaderRole.create(leader, role)
        } else {
            LeaderRole.findByLeaderAndRole(leader, role)?.delete()
        }

        def rtn = [success: true]
        render rtn as JSON

    }
}
