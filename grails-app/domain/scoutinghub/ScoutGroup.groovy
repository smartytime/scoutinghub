package scoutinghub

/**
 * A 'node' in the scouting organization.  ScoutGroups are hierarchical, and are categorized as follows:
 *
 * All units have a ScoutGroupType.  This is a fairly arbitrary assignment, but is allows a more concrete and
 * familiar association with a scouting organization.
 *
 * If groupType == ScoutGroupType.Unit, then an additional field is mapped: unitType.  A ScoutUnitType is one of Pack, Troop, Crew, or Team.
 *
 * Leaders are attached to ScoutGroup instances, which allows us to attach leaders at the council, district, and unit levels.  We can also
 * create somewhat arbitrary organizations (such as Ward, Stake, or Community) that we can attach permissions to.
 *
 */
class ScoutGroup implements Serializable {

    static searchable = {
//        id name: 'scoutGroupId'
        //groupType (propertyConverter:'enumConverter')
        except = ["childGroups", "leaderGroups"]
        parent(component: true, maxDepth: 6, prefix: "parent_")
    }

    /**
     * The 4-digit group unit identifier.  For non-units, this is usually some form of the group name.
     */
    String groupIdentifier

    /**
     * A readable label for this scouting group
     */
    String groupLabel
    ScoutGroup parent;

    ScoutGroupType groupType

    /**
     * Only required when groupType = Unit
     */
    ScoutUnitType unitType

    /**
     * Depth-first index of node's left value
     */
    Integer leftNode

    /**
     * Depth-first index of node's right value
     */
    Integer rightNode
    Date createDate;
    Date updateDate;

    static hasMany = [childGroups: ScoutGroup, leaderGroups: LeaderGroup]

    static constraints = {
        groupIdentifier(blank: false)
        groupLabel(blank: false)
        parent(validator: {val, ScoutGroup grp ->
            if (grp.groupType != ScoutGroupType.Council && grp.parent == null) {
                return ['scoutGroup.parent.required']
            }
        })
        leftNode(nullable: true)
        rightNode(nullable: true)
        unitType(nullable: true, validator: {val, ScoutGroup grp ->
            if (grp.groupType == ScoutGroupType.Unit && !grp.unitType) {
                return ['scoutGroup.unitType.required']
            }
        })
        groupType(nullable: true, validator: {val, ScoutGroup grp ->
            if (grp.groupType != ScoutGroupType.Unit && grp.unitType) {
                return ['scoutGroup.groupType.mustBeUnit']
            }
        })
        createDate nullable: true
        updateDate nullable: true

    }

    static mapping = {
        cache(true)
        leaderGroups(cascade: 'all-delete-orphan', sort: 'leader')
        childGroups(sort: "groupIdentifier")
        sort("groupIdentifier")
    }

    String toCrumbString() {
        List<String> names = []
        ScoutGroup unit = this;
        StringBuilder rtn = new StringBuilder()
        while (unit) {
            names << unit?.groupLabel ?: unit.groupIdentifier
            unit = unit.parent
        }

        ListIterator<String> namesIterator = names.listIterator(names.size())
        while (namesIterator.hasPrevious()) {
            rtn.append(namesIterator.previous())
            if (namesIterator.hasPrevious()) {
                rtn.append("&nbsp;>&nbsp;")
            }
        }
        return rtn.toString()
    }

    boolean canBeAdministeredBy(Leader leader) {
        boolean rtn
        if (leader?.hasRole("ROLE_ADMIN")) {
            rtn = true
        } else {
            LeaderGroup found = leader?.groups?.find {LeaderGroup lg ->
                return lg.scoutGroup.id == this.id && lg.admin
            }

            if (found) {
                rtn = true
            } else {
                rtn = parent?.canBeAdministeredBy(leader)
            }
        }
        return rtn
    }

    List<LeaderPositionType> findApplicablePositionTypes() {
        LeaderPositionType.values().findAll {LeaderPositionType type ->
            boolean matchesGroup = type.scoutGroupTypes.find {it == groupType} != null
            boolean matchesUnit = type.scoutUnitTypes.find {it == unitType} != null
            return matchesGroup || matchesUnit;

        }
    }

    @Override
    String toString() {
        String rtn = ""
        if (unitType) {
            rtn += "${unitType} ${groupIdentifier} - ${parent.groupLabel}"
        } else {
            rtn += groupLabel ?: groupIdentifier
        }

        return rtn
    }

    def beforeInsert = {
        createDate = new Date()
    }

    def beforeUpdate = {
        updateDate = new Date()
    }

    String groupOrUnitName() {
        return unitType?.name() ?: groupType?.name()
    }
}
