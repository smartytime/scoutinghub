package scoutinghub

class Leader implements Serializable {

    static searchable = {
        only: ['firstName', 'middleName', 'phone1', 'address1', 'lastName', 'email']
        myScoutingIds component: true
        groups component: true
    }

    String firstName
    String middleName
    String lastName
    String username
    String password
    String email
    String phone

    String address1
    String address2
    String city
    String state
    String postalCode

    String verifyHash
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    Date createDate;
    Date updateDate;

    /**
     * The date the user set up their account (and logged in) to the system.
     */
    Date setupDate

    static constraints = {
        username(nullable: true, unique: true)
        password(nullable: true)
        email(nullable: true, email: true)
        verifyHash(nullable: true)
        phone(nullable: true)
        createDate nullable: true
        updateDate nullable: true
        setupDate nullable: true

        middleName(nullable:true)

        address1(nullable:true)
        address2(nullable:true)
        city(nullable:true)
        state(nullable:true)
        postalCode(nullable:true)


    }

    static hasMany = [certificationClasses: CertificationClass,
            certifications: LeaderCertification,
            openIds: OpenID,
            myScoutingIds: MyScoutingId,
            groups: LeaderGroup]

    static mapping = {
        password column: '`password`'
        groups(cascade: 'all-delete-orphan', sort:'scoutGroup')
        certifications(sort:'dateEarned')
        sort('lastName')
        //myScoutingIds cascade: 'all-delete-orphan'
    }

    public void setPhone(String phone) {
        this.phone = phone?.findAll {it?.isNumber()}?.join("")
    }

    Set<Role> getAuthorities() {
        LeaderRole.findAllByLeader(this).collect { it.role } as Set
    }

    boolean hasAuthority(Role role) {
        return LeaderRole.findByLeaderAndRole(this, role) != null
    }

    boolean hasRole(String roleName) {
        return authorities.find {it.authority == roleName} != null
    }

    boolean hasCertification(Certification certification) {
        return findCertification(certification) != null
    }

    boolean certificationExpired(Certification certification) {
        return findCertification(certification)?.goodUntilDate()?.before(new Date())
    }

    LeaderCertification findCertification(Certification certification) {
        return certifications?.find {it.certification.id == certification?.id}
    }

    LeaderGroup findScoutGroup(ScoutGroup group) {
        return groups?.find {it.scoutGroup.id == group?.id}
    }

    boolean hasScoutGroup(ScoutGroup group) {
        return findScoutGroup(group) != null
    }

    boolean hasScoutingId(String scoutId) {
        boolean hasScoutingId = false
        myScoutingIds?.each {
            MyScoutingId myScoutingId ->
            if (myScoutingId.myScoutingIdentifier == scoutId) {
                hasScoutingId = true
            }
        }
        return hasScoutingId
    }

    boolean canAdminAtLeastOneUnit() {
        return groups?.find {it.admin} != null || hasRole("ROLE_ADMIN")
    }

    boolean canBeAdministeredBy(Leader leader) {
        boolean hasPermission = false
        if (leader?.hasRole("ROLE_ADMIN")) {
            hasPermission = true
        } else if(this.id == leader?.id) {
            hasPermission = true
        } else {
            this.groups?.collect {LeaderGroup leaderGroup -> leaderGroup.scoutGroup}?.each {ScoutGroup scoutGroup ->
                if (scoutGroup.canBeAdministeredBy(leader)) {
                    hasPermission = true
                }
            }
        }
        return hasPermission

    }

    Set<LeaderCertification> getLeaderCertifications() {
        LeaderCertification.findAllByLeader(this).collect {it.leaderCertification} as Set
    }

    @Override
    String toString() {
        String rtn = firstName + " "
        if(middleName) rtn += middleName + " "
        rtn += lastName
        return rtn
    }

    def beforeInsert = {
        createDate = new Date()
    }

    def beforeUpdate = {
        updateDate = new Date()
    }

}
