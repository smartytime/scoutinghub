package scoutinghub.infusionsoft

import scoutinghub.Leader

/**
 * User: eric
 * Date: 6/19/11
 * Time: 10:24 AM
 */
class InfusionsoftLeaderInfo {
    static mapWith = "neo4j"

    Leader leader
    int infusionsoftContactId
    static belongsTo = [Leader]
}
