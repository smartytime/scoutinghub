package scoutinghub

class LeaderGroup implements Serializable {

    static searchable = {
        root false
        only = ["scoutGroup", "admin", "leaderPosition", "pctTrained"]
        scoutGroup component: true
//        except = ["leader"]
    }

    Leader leader
    ScoutGroup scoutGroup
    boolean admin
    LeaderPositionType leaderPosition
    double pctTrained
    boolean registered

    Date createDate;
    Date updateDate;

    static constraints = {
        createDate(nullable: true)
        updateDate(nullable: true)
        scoutGroup(validator: {
            val, LeaderGroup obj->
            //If this is a unit, it's unique per group
            if(obj.scoutGroup.groupType == ScoutGroupType.Unit) {
                if(obj.leader.groups?.find{LeaderGroup grp->
                    grp.scoutGroup.id == obj.scoutGroup.id && grp.id != obj.id
                }) {
                    return ['leaderGroup.scoutGroup.unique']
                }
            } else {
                if(obj.leader.groups?.find{LeaderGroup grp->
                    grp.scoutGroup.id == obj.scoutGroup.id && grp.id != obj.id && grp.leaderPosition == obj.leaderPosition
                }) {
                    return ['leaderGroup.scoutGroup.unique']
                }
            }
        })
    }

    def beforeInsert = {
        createDate = new Date()
        updateDate = new Date()
    }

    def beforeUpdate = {
        updateDate = new Date()
    }

}
