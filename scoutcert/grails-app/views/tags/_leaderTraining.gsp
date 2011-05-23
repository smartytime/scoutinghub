<%@ page import="scoutcert.CertificationStatus" %>
<div class="profileCertificationContainer ${request.currClass}">

    <g:if test="${certificationInfo.certificationStatus == CertificationStatus.Expired}">

        <div class="profileCertification">
            <div class="trainingStatus">
                <div class="headerText">${certificationInfo.certification.name}</div>

                <div class="trainingDetails">
                    <g:message code="leader.profile.trainingExpiredOn"/> <g:formatDate
                            date="${certificationInfo.leaderCertification.goodUntilDate()}" format="MM-dd-yyyy"/>
                    <a href="javascript:markTrainingComplete(${certificationInfo.leader.id}, ${certificationInfo.leaderCertification.certification.id})"><g:message
                            code="leader.profile.alreadyComplete"/></a>
                </div>
            </div>
        </div>

    </g:if>

    <g:elseif test="${certificationInfo.certificationStatus == CertificationStatus.Missing}">
        <div class="profileCertification training-missing">
            <div class="trainingStatus">
                <div class="training-title">${certificationInfo.certification.name}</div>

                <div class="training-details missingTraining">
                    <g:message code="leader.profile.missingTraining"/>&nbsp;
                    <a href="javascript:markTrainingComplete(${certificationInfo.leader.id},
                ${certificationInfo.certification.id})"><g:message code="leader.profile.alreadyComplete"/></a>
                </div>
            </div>
        </div>
    </g:elseif>
    <g:else>
        <g:set var="certification"
               value="${certificationInfo.leader.findCertification(certificationInfo.certification)}"/>
        <div class="fldContainer ui-corner-all profileCertification validation successIcon shadow">
            <div class="headerText">${certificationInfo.certification.name}</div>

            <div class="trainingDetails">Good until <g:formatDate
                    date="${certificationInfo.leaderCertification.goodUntilDate()}" format="MM-dd-yyyy"/><br/>
                <g:message
                        code="${certificationInfo.leaderCertification.enteredType}.label"/> ${certificationInfo.leaderCertification.enteredBy} <g:formatDate
                        date="${certificationInfo.leaderCertification.dateEntered}" format="MMM yyyy"/>
            </div>

        </div>
    </g:else>


    <div class="upcomingTrainings"></div>
</div>
