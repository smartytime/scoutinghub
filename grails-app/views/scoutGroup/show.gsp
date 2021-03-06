<%@ page import="scoutinghub.ScoutGroup" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${layoutName}"/>
    <title>${scoutGroupInstance}</title>
</head>

<body>

<g:form>
    <s:content>
        <s:section>
            <g:if test="${flash.message}">
                <s:msg code="${flash.message}" type="info"/>
            </g:if>

            <s:sectionHeader icon="units-icon" code="${scoutGroupInstance.toString()}">
                <s:ctxmenu>
                    <g:ctxmenuItem controller="scoutGroup" action="edit" id="${scoutGroupInstance?.id}" iconType="edit"
                                   code="Edit"/>
                </s:ctxmenu>
            </s:sectionHeader>
            <s:propertyList class="vertical-form">
                <s:property class="alternate-color" code="${message(code:'scoutGroup.parent.label')}">
                    <g:if test="${scoutGroupInstance?.parent}">
                        <g:link controller="scoutGroup" action="show"
                                id="${scoutGroupInstance?.parent?.id}">${scoutGroupInstance?.parent}</g:link>
                    </g:if>
                    <g:else>
                        <g:message code="None"/>
                    </g:else>
                </s:property>

                <s:property code="scoutGroup.groupType.label"><g:message
                        code="${scoutGroupInstance?.groupType}.label"/></s:property>
                <s:property class="alternate-color" code="scoutGroup.unitType.label"><g:message
                        code="${scoutGroupInstance?.unitType}.label"/></s:property>
                <s:property code="scoutGroup.groupIdentifier.label">${scoutGroupInstance?.groupIdentifier}</s:property>
                <s:property class="alternate-color" code="scoutGroup.trainingReport.label">
                    <g:link controller="training" action="trainingReport" id="${scoutGroupInstance?.id}"><g:message
                            code="scoutGroup.trainingReport.view"/></g:link>
                </s:property>
                <s:property code="scoutGroup.printableReport.label">
                    <g:link onclick="alert('${message(code:'training.detailedReport.patience')}')" controller="training" action="detailedReport" id="${scoutGroupInstance?.id}"><g:message
                            code="scoutGroup.printableReport.view"/></g:link>
                </s:property>

            </s:propertyList>

        </s:section>

        <g:if test="${scoutGroupInstance.childGroups?.size() > 0}">
            <s:section>
                <s:sectionHeader code="scoutGroup.childUnits" icon="units-icon"/>
                <s:propertyList class="vertical-form">
                    <g:each in="${scoutGroupInstance.childGroups}" var="childGroup" status="i">
                        <s:property class="${i % 2 ==0 ? 'alternate-color' : ''}"
                                    code="${childGroup.unitType ?: childGroup.groupType}.label">
                            <g:link class="leaderProfileLink" controller="scoutGroup" action="show"
                                    id="${childGroup?.id}">${childGroup}</g:link>
                        </s:property>
                    </g:each>
                </s:propertyList>

            </s:section>

        </g:if>


        <s:section>
            <s:sectionHeader code="scoutGroup.leaders" icon="units-icon">
                <s:ctxmenu>
                    <g:ctxmenuItem controller="leader" action="create" params="['scoutGroup.id':scoutGroupInstance?.id]"
                                   args="[scoutGroupInstance.toString()]" iconType="add" code="scoutGroup.addLeader"/>
                </s:ctxmenu>
            </s:sectionHeader>
            <s:propertyList class="vertical-form">
                <g:each in="${scoutGroupInstance.leaderGroups}" var="leader" status="leaderStatus">

                    <s:property class="${leaderStatus %2 == 0 ? 'alternate-color':''}"
                                code="${leader.leaderPosition}.label">
                        <g:link class="leaderProfileLink" controller="leader" action="view"
                                id="${leader?.leader?.id}">${leader.leader}</g:link>
                    </s:property>

                </g:each>
            </s:propertyList>

        </s:section>

    </s:content>

</g:form>
</body>
</html>
