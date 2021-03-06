package scoutinghub

/**
 * Record that a Leader has completed a given certification.
 */
class LeaderCertification implements Serializable {

    transient TrainingService trainingService
    static transients = ["trainingService"]

    Date dateEarned
    Date expirationDate
    Certification certification

    Leader leader
    Leader enteredBy

    Date dateEntered
    Date lastCalculationDate
    LeaderCertificationEnteredType enteredType

    Date createDate;
    Date updateDate;

    static belongsTo = [Certification, Leader]
    static constraints = {
        lastCalculationDate(nullable: true)
        createDate(nullable: true)
        updateDate(nullable: true)
        expirationDate(nullable: true)
        certification(unique: 'leader')
    }

    static mapping = {
        sort 'dateEarned'
    }

    boolean hasExpired() {
        return goodUntilDate()?.before(new Date())
    }

    Date goodUntilDate() {
        Date rtn = null
        if (certification?.durationInDays > 0 && dateEarned) {
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(dateEarned)
            calendar.add(Calendar.DATE, certification.durationInDays)
            rtn = calendar.time
        }

        return rtn
    }

    def beforeInsert = {
        createDate = new Date()
        expirationDate = goodUntilDate();

    }

    def beforeUpdate = {
        updateDate = new Date()
        expirationDate = goodUntilDate();
    }

    @Override
    String toString() {
        return "${leader}: ${certification}"
        //return "leader: certification"
    }


}
