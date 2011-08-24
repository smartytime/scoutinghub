<%@ page import="scoutinghub.Role" %>
<html>
<head>
    <title><g:message code="menu.leader.profile"/></title>
    <g:if test="${session.isMobile == true}">
        <meta name="layout" content="iwebkit"/>
    </g:if>
    <g:else>
        <meta name="layout" content="main"/>
    </g:else>
    <script type="text/javascript">

        function togglePermission(checkbox, leaderid, roleid) {
            jQuery.ajax({
                url:"/scoutinghub/permissions/setPermission",
                data: {checked: checkbox.checked, leaderId:leaderid, roleId: roleid}
            });
        }

        function enterTrainingDetails(id) {
            jQuery("#completeTraining" + id).click();
        }

        function ignoreDuplicate(leaderIdA, leaderIdB) {
            jQuery.getJSON("/scoutinghub/leader/ignoreDuplicate", {leaderA:leaderIdA, leaderB:leaderIdB}, function(json) {
                if (json.success) {
                    window.location.reload();
                }
            });
        }

        jQuery(document).ready(function() {
            var pct = parseInt(jQuery(this).attr("pct"));
            jQuery("#trainingCompletion").progressbar({value:pct});

            jQuery(".leader-unit").mouseover(
                    function() {
                        jQuery(".remove-button", this).show();
                    }
            ).mouseout(function() {
                        jQuery(".remove-button", this).hide();
                    });

            jQuery(".profileCertificationContainer").each(function() {
                var jthis = jQuery(this);
                var certificationId = parseInt(jthis.attr("certificationId"));
                var leaderId = parseInt(jthis.attr("leaderId"));

                if (certificationId > 0) {
                    jQuery.get("/scoutinghub/certificationClass/findByCertification", {certificationId:certificationId, leaderId:leaderId},
                            function(data) {
                                if (data) {
                                    jthis.find(".upcomingTrainings").append("<div class='currentTraining ui-corner-all'>" + data + "</div>");

                                }
                            });
                }
            });

        });
    </script>

</head>

<body>

<s:content class="floatContent profile">

%{--No equivalent of jsp:attribute in jsp, so there's no way to do it later --}%
<g:set var="menu" scope="request">
    <li><g:link action="foo">Edit My Profile</g:link></li>
</g:set>

<g:if test="${duplicates?.size()}">
    <s:browser>
        <s:section>
            <s:msg type="warning">
                <div class="msg1">
                    <g:message code="leader.profile.duplicate"/>
                </div>

                <div class="msg2">
                    <g:message code="leader.profile.duplicate2"/>
                    <b>
                        <g:message code="leader.profile.duplicate_verify"/>
                    </b>
                </div>

                <div class="msg2">
                    <div class="duplicate-results">
                        <div class="header">
                            <div>Name</div>

                            <div>Phone</div>

                            <div>Email</div>

                            <div>Address</div>

                            <div>BSA ID</div>

                            <div></div>

                            <div></div>
                        </div>
                        <g:each in="${duplicates}" var="duplicate">

                            <div>
                                <div>${duplicate}</div>

                                <div>${f.formatPhone(phone: duplicate.phone) ?: "No Phone"}</div>

                                <div>${duplicate.email ?: "No Email"}</div>
                                <div>${duplicate.address1 ?: "No Address"}</div>

                                <div>
                                    <g:if test="${duplicate.myScoutingIds?.size() > 0}">
                                        ${duplicate.myScoutingIds?.iterator()?.next()}
                                    </g:if>
                                    <g:else>No BSA ID</g:else>

                                </div>

                                <div><a href="javascript:openMergeLeaderDialog(${duplicate.id},${leader.id})">Definitely a Match</a>
                                </div>

                                <div><a href="javascript:ignoreDuplicate(${leader.id}, ${duplicate.id})">Not a Match</a>
                                </div>
                            </div>

                        </g:each>
                    </div>

                </div>

            </s:msg>
        </s:section>
    </s:browser>
</g:if>
<g:else>
    <g:if test="${leader.certifications?.size() == 0 && leader.myScoutingIds?.size() == 0}">
        <s:section>
            <s:msg type="warning">
                <div class="msg1"><g:message code="leader.profile.nolink"/></div>

                <div class="msg2">
                    <g:message code="leader.profile.nolinkdescription"/>
                    <ul class="list">
                        <li>
                            <strong>
                                <g:link title="${message(code:'leader.profile.addScoutingId')}" lbwidth="500"
                                    class="lightbox"
                                    controller="myScoutingId" action="create" params="['leader.id':leader.id]">
                                    <g:message code="leader.profile.nolinkdescription_item1"/>
                                </g:link>
                            </strong>
                        </li>
                        <li>
                            <strong><g:link controller="leader" action="view" id="${leader.id}" params="[edit:true]"><g:message code="leader.profile.nolinkdescription_item2"/></g:link></strong>
                        </li>
                        <li>
                           <g:message code="leader.profile.nolinkdescription_item3"/>
                        </li>
                    </ul>
                </div>

            </s:msg>
        </s:section>
    </g:if>
</g:else>


<g:form action="saveProfile">
    <s:section class="floatSection myprofile">
        <s:sectionHeader icon="profile-icon" code="leader.profile.myprofile">
            <s:ctxmenu>
                <g:ctxmenuItem img="edit-icon" controller="leader" action="view" id="${leader.id}"
                               params="[edit:true]" iconType="edit" code="leader.profile.edit">
                %{--<s:linker style="white-space:nowrap;" action="view" id="${leader.id}" params="[edit:true]">--}%
                %{--<g:inlineIcon class="edit-icon"/>--}%
                %{--<g:ctxmenuLabel>--}%
                %{--<g:message code="leader.profile.edit"/>--}%
                %{--</g:ctxmenuLabel>--}%
                %{--</s:linker>--}%
                </g:ctxmenuItem>
                <g:ctxmenuItem>
                    <s:linker img="add-icon" title="${message(code:'leader.profile.addScoutingId')}" lbwidth="500"
                              class="lightbox"
                              controller="myScoutingId" action="create" params="['leader.id':leader.id]">
                        <g:inlineIcon class="add-icon"/>
                        <g:ctxmenuLabel><g:message code="leader.profile.addAnother"/></g:ctxmenuLabel>
                    </s:linker>
                </g:ctxmenuItem>
            </s:ctxmenu>
        </s:sectionHeader>


        <g:set var="menu" value="" scope="request"/>
        <s:propertyList class="edit-profile">
            <g:if test="${params.edit}">
                <g:hasErrors bean="${flash.leaderError}">
                    <s:msg type="error">
                        <g:renderErrors bean="${flash.leaderError}"/>
                    </s:msg>
                </g:hasErrors>

                <g:hiddenField name="id" value="${leader.id}"/>
                <s:div class="alternate-color">
                    <s:textField name="firstName" code="leader.firstName.label" value="${leader?.firstName}"/>
                    <s:textField name="address1" code="leader.address1.label" value="${leader?.address1}"/>
                </s:div>
                <s:div class="alternate-color">
                    <s:textField name="middleName" code="leader.middleName.label" value="${leader?.middleName}"/>
                    <s:textField name="address2" code="leader.address2.label" value="${leader?.address2}"/>

                </s:div>
                <s:div class="alternate-color">
                    <s:textField name="lastName" code="leader.firstName.label" value="${leader?.lastName}"/>
                    <s:textField name="city" code="leader.city.label" value="${leader?.city}"/>
                </s:div>

                <s:div class="alternate-color">
                    <s:textField name="email" code="leader.email.label" value="${leader?.email}"/>
                    <s:textField name="state" code="leader.state.label" value="${leader?.state}"/>
                </s:div>

                <s:div class="alternate-color">
                    <s:textField name="phone" code="leader.phone.label" value="${f.formatPhone(phone: leader?.phone)}"/>
                    <s:textField name="postalCode" code="leader.postalCode.label" value="${leader?.postalCode}"/>
                </s:div>



                <s:div>
                    <s:submit name="submit" value="${message(code:'Save')}"/>
                </s:div>

            </g:if>

            <g:else>

                <s:div class="alternate-color prop-container">
                    <s:property code="leader.name.label">${leader}</s:property>
                    <s:property
                            code="leader.email.label">${leader?.email ?: message(code: 'leader.email.noneFound')}</s:property>

                </s:div>

                <s:div class="prop-container">
                    <s:property
                            code="leader.phone.label">${f.formatPhone(phone: leader?.phone) ?: message(code: 'leader.phone.noneFound')}</s:property>
                    <s:property code="leader.address.label">${leader?.address1 ?: "Not set"}</s:property>
                </s:div>


                <s:div class="alternate-color prop-container">

                    <s:property code="leader.profile.scoutingids">
                        <g:if test="${leader?.myScoutingIds?.size()}">
                            <g:each in="${leader.myScoutingIds}" var="myScoutingId">
                                <div class="myId">${myScoutingId.myScoutingIdentifier}</div>
                            </g:each>

                        </g:if>
                        <g:else>
                            <g:link title="${message(code:'leader.profile.addScoutingId')}" lbwidth="500"
                                    class="lightbox"
                                    controller="myScoutingId" action="create" params="['leader.id':leader.id]">
                                <g:message code="leader.profile.noneYet"/>
                            </g:link>

                        </g:else>

                    </s:property>

                    <s:property code="leader.setupDate.label">
                        <g:if test="${leader?.setupDate}">
                            <g:formatDate date="${leader?.setupDate}" format="MM-dd-yyyy"/>
                        </g:if>
                        <g:else>
                            Not Set Up Yet
                        </g:else>
                    </s:property>

                </s:div>
            </g:else>
        </s:propertyList>
    </s:section>
</g:form>

<s:section class="floatSection">
    <s:sectionHeader icon="units-icon" code="leader.profile.groups">
        <s:ctxmenu>
            <g:ctxmenuItem>
                <s:linker img="edit-icon" class="lightbox" title="${message(code:'leader.profile.addToGroup')}"
                          lbwidth="475"
                          controller="leaderGroup" action="create" params="['leader.id': leader.id]">
                    <g:inlineIcon class="edit-icon"/>
                    <g:ctxmenuLabel>
                        <g:message code="leader.profile.addAnotherUnit" args="[leader.firstName]"/>
                    </g:ctxmenuLabel>
                </s:linker>
            </g:ctxmenuItem>

            <g:ctxmenuItem>
                <s:linker img="add-icon" title="${message(code: 'leaderGroup.permissions', args:[leader])}"
                          controller="leaderGroup" action="permissions" id="${leader.id}" class="lightbox">
                    <g:inlineIcon class="add-icon"/>
                    <g:ctxmenuLabel>
                        <g:message code="leader.profile.editPermission" args="[leader?.firstName]"/>
                    </g:ctxmenuLabel>
                </s:linker>
            </g:ctxmenuItem>

        </s:ctxmenu>
    </s:sectionHeader>
    <s:propertyList>
        <g:if test="${leader?.groups?.size()}">
            <g:each in="${leader.groups}" var="group" status="i">
                <g:if test="${i%2 == 0}">
                    <g:if test="${currClass == 'alternate-color'}">
                        <g:set var="currClass" value=""/>
                    </g:if>
                    <g:else>
                        <g:set var="currClass" value="alternate-color"/>
                    </g:else>
                </g:if>
                <s:leaderUnit leaderGroup="${group}" code="${group?.leaderPosition}.label" class="${currClass}">
                    ${group?.scoutGroup}

                    <p:canAdministerGroup leader="${leader}"
                                          scoutGroup="${group?.scoutGroup}">(admin)</p:canAdministerGroup>
                    <p:canAdministerGroup scoutGroup="${group?.scoutGroup}">
                        <div><g:link class="manage-this-unit" controller="scoutGroup" action="show"
                                     id="${group?.scoutGroup?.id}">
                            <g:message code="scoutGroup.manage"
                                       args="[group?.scoutGroup?.groupType?.name()?.humanize()]"/>
                        </g:link></div>
                    </p:canAdministerGroup>
                </s:leaderUnit>
                <g:set var="grpI" value="${i}" scope="request"/>
            </g:each>
            <g:if test="${request.grpI%2==0}">
                <s:leaderUnit class="${currClass}"/>
            </g:if>
        </g:if>
        <g:else>
            <s:property>
                <g:link class="lightbox" title="${message(code:'leader.profile.addToGroup')}" lbwidth="600"
                        controller="leaderGroup" action="create" params="['leader.id': leader.id]">
                    <g:message code="leader.profile.noneYet" args="[leader.firstName]"/>
                </g:link>
            </s:property>
        </g:else>
    </s:propertyList>
</s:section>

<g:if test="${certificationInfo?.size() > 0}">
    <s:section class="floatSection">
        <s:sectionHeader code="leader.profile.mytraining" icon="training-icon"/>

        <g:if test="${!certificationInfo}">
            <s:msg type="warning" code="leader.profile.notInUnit"/>
        </g:if>

        <g:set scope="request" var="certIndex" value="${0}"/>
        <g:each in="${certificationInfo}" var="certification">
            <g:if test="${request.certIndex%2 == 0}">

                <g:if test="${request.currClass == 'alternate-color'}">
                    <g:set var="currClass" value="" scope="request"/>
                </g:if>
                <g:else>
                    <g:set var="currClass" value="alternate-color" scope="request"/>
                </g:else>
            </g:if>
            <g:set var="certIndex" value="${request.certIndex+1}" scope="request"/>
            <s:leaderTraining certificationInfo="${certification}"/>
        </g:each>

        <g:if test="${request.certIndex%2==1}">
            <s:div class="profileCertificationContainer ${request.currClass}"><s:div
                    class="profileCertification"/></s:div>
        </g:if>

    </s:section>
</g:if>

<s:section class="floatSection">
    <s:sectionHeader code="leader.profile.myothertraining" icon="training-icon">
        <s:ctxmenu>
            <g:ctxmenuItem iconType="add">
                <s:linker img="edit-icon" class="lightbox"
                          title="${message(code:'leader.profile.addAdditionalLinkCtx')}"
                          lbwidth="600"
                          controller="leaderCertification" action="create" params="['leader.id': leader.id]">
                    <g:inlineIcon class="add-icon"/>
                    <g:ctxmenuLabel>
                        <g:message code="leader.profile.addAdditionalLinkCtx"/>
                    </g:ctxmenuLabel>
                </s:linker>
            </g:ctxmenuItem>

        </s:ctxmenu>
    </s:sectionHeader>

    <g:if test="${!extraCertificationInfo}">
        <s:text>
            <g:message code="leader.profile.addAdditional"/>
            <s:linker img="edit-icon" class="lightbox" title="${message(code:'leader.profile.addAdditionalLink')}"
                      lbwidth="600"
                      controller="leaderCertification" action="create" params="['leader.id': leader.id]">
                <g:message code="leader.profile.addAdditionalLink"/>
            </s:linker>
        </s:text>
    </g:if>

    <g:set scope="request" var="certIndex" value="${0}"/>
    <g:each in="${extraCertificationInfo}" var="certification">
        <g:if test="${request.certIndex%2 == 0}">

            <g:if test="${request.currClass == 'alternate-color'}">
                <g:set var="currClass" value="" scope="request"/>
            </g:if>
            <g:else>
                <g:set var="currClass" value="alternate-color" scope="request"/>
            </g:else>
        </g:if>
        <g:set var="certIndex" value="${request.certIndex+1}" scope="request"/>
        <s:leaderTraining certificationInfo="${certification}"/>
    </g:each>

    <g:if test="${request.certIndex%2==1}">
        <s:div class="profileCertificationContainer ${request.currClass}"><s:div
                class="profileCertification"/></s:div>
    </g:if>

</s:section>
</s:content>

</body>
</html>