<%@ page import="scoutinghub.CertificationStatus" %>
<div class="profileCertificationContainer ${request.currClass}" leaderId="${certificationInfo?.leader?.id}"
     certificationId="${certificationInfo?.certificationStatus == CertificationStatus.Current ? "0" : certificationInfo?.certification?.id ?: '0'}">

    <g:if test="${certificationInfo.certificationStatus == CertificationStatus.Expired}">

        <div class="profileCertification training-incomplete">
            <div class="trainingStatus training-title" data-title="Training Details" data-selector="#tt${certificationInfo.certification.id}">
                <h4>
                    <i class="icon-heart"></i>
                    ${certificationInfo.certification.name}
                </h4>

                <div class="training-details missing-training">
                    <g:message code="leader.profile.trainingExpiredOn"/>:&nbsp;<g:formatDate
                            date="${certificationInfo.leaderCertification.goodUntilDate()}" format="MM-dd-yyyy"/>&nbsp;
                    <f:completeTrainingLink certificationInfo="${certificationInfo}">
                        <g:message code="leader.profile.alreadyComplete"/>
                    </f:completeTrainingLink>

                    <div class="upcomingTrainings"></div>

                </div>
            </div>
        </div>

    </g:if>

    <g:elseif test="${certificationInfo.certificationStatus == CertificationStatus.Missing}">
        <div class="profileCertification training-incomplete">
            <div class="trainingStatus">
                <div class="training-title" data-title="Training Details" data-selector="[id='tt${certificationInfo.certification.id}']">
                    <h4>
                        <i class="training-status red icon-thumbs-down"></i>
                        ${certificationInfo.certification.name}
                    </h4>
                </div>

                <div id="tt${certificationInfo.certification.id}">
                    <div class="training-details missing-training">
                        <h5>
                        <g:message code="leader.profile.missingTraining"/>&nbsp;
                        <f:completeTrainingLink certificationInfo="${certificationInfo}">
                            <g:message code="leader.profile.alreadyComplete"/>
                        </f:completeTrainingLink>
                        <div class="upcomingTrainings"></div>

                        </h5>
                    </div>
                </div>
            </div>
        </div>
    </g:elseif>
    <g:else>
        <g:set var="certification"
               value="${certificationInfo.leader.findCertification(certificationInfo.certification)}"/>
        <div class="profileCertification training-complete">
            <div class="training-title"  data-title="Training Details" data-selector="#tt${certificationInfo.certification.id}">
                <h4>
                    <i class="training-status green icon-thumbs-up"></i>
                    ${certificationInfo.certification.name}
                </h4>
            </div>

            <div class="training-details" id="tt${certificationInfo.certification.id}">
                <h5>
                <g:if test="${certificationInfo.leaderCertification.goodUntilDate()}">
                    Good until <g:formatDate
                        date="${certificationInfo.leaderCertification.goodUntilDate()}" format="MM-dd-yyyy"/>&nbsp;
                    <f:completeTrainingLink certificationInfo="${certificationInfo}">
                        <g:message code="leader.profile.alreadyComplete"/>
                    </f:completeTrainingLink>

                </g:if>
                <g:else>
                    <g:message code="leader.profile.goodForever" args="[g.formatDate(format: 'MM-dd-yyyy', date:certificationInfo.leaderCertification.dateEarned)]"/>&nbsp;
                    <f:completeTrainingLink certificationInfo="${certificationInfo}">
                        <g:message code="leader.profile.alreadyComplete"/>
                    </f:completeTrainingLink>

                </g:else>

                <br/>
                <g:message
                        code="${certificationInfo.leaderCertification.enteredType}.label"/> ${certificationInfo.leaderCertification.enteredBy} <g:formatDate
                        date="${certificationInfo.leaderCertification.dateEntered}" format="MMM yyyy"/>
                </h5>

            </div>

        </div>
    </g:else>

</div>
