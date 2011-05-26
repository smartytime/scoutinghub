package scoutinghub.trainingImport

import grails.validation.ValidationException
import java.text.DecimalFormat
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.hibernate.Session
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.validation.ObjectError
import scoutinghub.Leader
import scoutinghub.LeaderService
import scoutinghub.ScoutGroup

import scoutinghub.ScoutUnitType
import scoutinghub.Certification
import scoutinghub.LeaderCertification
import scoutinghub.LeaderCertificationEnteredType
import scoutinghub.LeaderPositionType
import scoutinghub.ScoutGroupService
import scoutinghub.TrainingService

class ImportTrainingService {

    //Maybe it should be?
    static transactional = true

    LeaderService leaderService
    ScoutGroupService scoutGroupService
    TrainingService trainingService

    def searchableService

    /**
     * Eventually, this map may need to be customizable on a per-spreadsheet configuration.  For the spreadsheets we've
     * seen,
     */
    def headerMap = [
            "PID#": "scoutingId",
            "FirstName": "firstName",
            "LastName": "lastName",
            "Email": "email",
            "Unit#": "unitNumber",
            "UnitType": "unitType",
            "PositionCode": "position",
            "Position": "position",
            "YPT": "yptDate",
            "ThisIsScouting": "thisIsScoutingDate",
            "FastStart": "fastStartDate",
            "LeaderSpecific": "leaderSpecificDate",
            "IntroOutdoorSkills": "outdoorSkillsDate",
            "Y02CrewsOnly": "y02CrewSkillsDate",
            "EffectiveDate": "effectiveDate"
    ]

    def certDefinitionMap = [
            "yptDate": "Y01",
            "y02CrewSkillsDate": "Y02",
            "thisIsScoutingDate": "WA01",
            "outdoorSkillsDate": "S11"
    ]

    def cubmasterSpecificMap = [
            "leaderSpecificDate": "C40",
            "fastStartDate": "CF3"
    ]

    def scoutmasterSpecificMap = [
            "leaderSpecificDate": "S24",
            "fastStartDate": "SFS"
    ]

    def varsityCoachSpecificMap = [
            "leaderSpecificDate": "V21",
            "fastStartDate": "VFS"
    ]

    def crewAdviserSpecificMap = [
            "leaderSpecificDate": "P21",
            "fastStartDate": "PFS"
    ]

    def positionTypeToSpecificMap = [
        (LeaderPositionType.Cubmaster):cubmasterSpecificMap,
        (LeaderPositionType.Scoutmaster):scoutmasterSpecificMap,
        (LeaderPositionType.VarsityCoach):varsityCoachSpecificMap,
        (LeaderPositionType.CrewAdvisor):crewAdviserSpecificMap
    ]

    def leaderPositionTypeMap = [
            "CharterRep": LeaderPositionType.CharterRep,
            "CharteredOrganizationRep": LeaderPositionType.CharterRep,
            "CommitteeChair": LeaderPositionType.CommitteeChair,
            "CommitteeChairman": LeaderPositionType.CommitteeChair,
            "CommitteeMember": LeaderPositionType.CommitteeMember,
            "Scoutmaster": LeaderPositionType.Scoutmaster,
            "AssistantScoutMaster": LeaderPositionType.AssistantScoutMaster,
            "AssistantScoutmaster": LeaderPositionType.AssistantScoutMaster,
            "Cubmaster": LeaderPositionType.Cubmaster,
            "AssistantCubmaster": LeaderPositionType.AssistantCubmaster,
            "TigerLeader": LeaderPositionType.TigerLeader,
            "TigerCubDenLeader": LeaderPositionType.TigerLeader,
            "DenLeader": LeaderPositionType.DenLeader,
            "PackTrainer": LeaderPositionType.PackTrainer,
            "WebelosLeader": LeaderPositionType.WebelosLeader,
            "AssistantDenLeader": LeaderPositionType.AssistantDenLeader,
            "AsstDenLeader": LeaderPositionType.AssistantDenLeader,
            "AssistantWebelosLeader": LeaderPositionType.AssistantWebelosLeader,
            "AsstWebelosLeader": LeaderPositionType.AssistantWebelosLeader,
            "VarsityScoutCoach": LeaderPositionType.VarsityCoach,
            "AssistantVarsityCoach": LeaderPositionType.AssistantVarsityCoach,
            "VenturingCrewAdvisor": LeaderPositionType.CrewAdvisor,
            "AssistantCrewAdvisor": LeaderPositionType.AssistantCrewAdvisor,
            "VenturingCrewAssocAdvisor": LeaderPositionType.AssistantCrewAdvisor

    ]

    /**
     * @todo: eric: I think the Y02CrewsOnly has a different name in other spreadsheets, more research is necessary
     */
    def optionalFields = ["Email", "Y02CrewsOnly", "EffectiveDate"]

    /**
     * The POI libraries don't provide an easy way to retrieve the value of a cell as the underlying object it represents (like
     * and rst.getObject call).  Because of this, we'll need to know what type of data we're expecting, and this closure provides
     * the logic to retrieve the data correctly
     *
     * @todo Research whether the cellType logic changes for the newer xml-based formats
     */
    def stringClosure = {
        def rtn
        if (it?.cellType == 0) { //A number
            rtn = new DecimalFormat("############################").format(it?.numericCellValue)
        } else {
            rtn = it?.stringCellValue
        }
        rtn = rtn?.trim()
        if (rtn == "") rtn = null
        return rtn
    }

    /**
     * The POI libraries don't provide an easy way to retrieve the value of a cell as the underlying object it represents (like
     * and rst.getObject call).  Because of this, we'll need to know what type of data we're expecting, and this closure provides
     * the logic to retrieve the data correctly
     *
     * @todo Research whether the cellType logic changes for the newer xml-based formats
     */
    def dateClosure = {
        def rtn = null
        try {
            if (it?.cellType == 3 || it?.cellType == 1) { //String
                if (it?.stringCellValue?.trim() != "") {
                    //@todo handle this error case
                }
            } else {
                rtn = it?.dateCellValue
            }
        } catch (Exception e) {
            return null;
        }
        return rtn
    }

    /**
     * This determines what type of data will be returned for each column
     */
    def dataTypeMap = [
            "PID#": stringClosure,
            "FirstName": stringClosure,
            "LastName": stringClosure,
            "Email": stringClosure,
            "Unit#": stringClosure,
            "UnitType": stringClosure,
            "PositionCode": stringClosure,
            "Position": stringClosure,
            "YPT": dateClosure,
            "ThisIsScouting": dateClosure,
            "FastStart": dateClosure,
            "LeaderSpecific": dateClosure,
            "IntroOutdoorSkills": dateClosure,
            "Y02CrewsOnly": dateClosure,
            "EffectiveDate": dateClosure
    ]

    /**
     * Called right when the job is configured.  It does a few things:
     *
     * 1.  Figures out which sheets will be imported
     * 2.  Maps the header indexes (which column contains which data for each sheet)
     * 3.  How many rows of importable data exist on each sheet
     */
    void setupImportJob(ImportJob importJob) {
        importJob.sheetsToImport.clear()
        importJob.currentSheet = null
        for (int i = 0; i < importJob.workbook.numberOfSheets; i++) {

            Sheet currentSheet = importJob.workbook.getSheetAt(i)

            //Map header indexes
            def headerIndex = [:]
            Row headerRow = locateHeaderRow(currentSheet)
            if (!headerRow) throw new IllegalStateException("This sheet doesn't appear to contain the headers")
            //Let's verify the headers
            for (Cell cell: headerRow) {
                String cellValue = getModifiedHeaderValue(cell.getStringCellValue())
                if (cellValue != "" && headerMap.containsKey(cellValue)) {
                    headerIndex[cellValue] = cell.columnIndex
                }

            }

            //Figure out how many importable rows there are
            int scoutingIdIndex = headerIndex["PID#"]
            int rowCount = 0;
            for (Row row: currentSheet) {
                if (containsDataToImport(row, scoutingIdIndex)) {
                    rowCount++
                }
            }

            //Schedule a sheet to be imported with the parent job
            ImportSheet sheet = new ImportSheet(
                    sheetName: importJob.workbook.getSheetName(i),
                    importStatus: ImportStatus.Waiting,
                    totalToImport: rowCount,
                    workbookSheet: currentSheet,
                    index: i,
                    headerRow: headerRow,
                    pidIndex: scoutingIdIndex,
                    headerIndex: headerIndex
            )
            importJob.sheetsToImport << sheet
        }
    }

    /**
     * Scans down a sheet looking for a row that appears to contain header data.  Will give up after 5 rows, and determines
     * success when at least 5 of the desired headers are found.  Didn't require all headers to be there because there is
     * we want to be able to report that the sheet is missing a couple of headers (instead of failing altogether)
     * @todo This method returns null when no header row is found.  Make sure places that call it are handling appropriately
     * @param sheet
     * @return
     */
    Row locateHeaderRow(Sheet sheet) {

        Row rtn = null
        for (i in 0..5) { //Don't look at more than 5 rows
            Row row = sheet.getRow(i)
            try {

                //Look for mapped headers in the row (found more than 5 headers??)
                int foundCells = 0
                for (Cell cell: row) {
                    if (cell.cellType != HSSFCell.CELL_TYPE_BLANK) { //Ignore blank cells
                        String cellValue = getModifiedHeaderValue(cell.getStringCellValue())
                        if (cellValue != "" && headerMap.containsKey(cellValue)) {
                            foundCells++;
                        }
                    }
                }
                if (foundCells > 5) {
                    rtn = row;
                    break;
                }
            } catch (Exception e) {
                //Most common exception would be when looking for string data (headers) when the row contains numerical
                //data.
                //@todo: eric: That case should probably be checked above.
                //At the end of the day, an error will cause this to continue - eventually
                e.printStackTrace()
            }
        }
        return rtn;
    }

    /**
     * There are often whitespace conflicts when dealing with header names.  I chose to remove all whitespace
     * @todo it should probably also ignore case
     *
     * @param originalValue
     * @return
     */
    String getModifiedHeaderValue(String originalValue) {
        return originalValue?.replaceAll("\\s", "") ?: ""
    }

    /**
     * Determines whether or not a row has importable data.  Currently, this is defined by the fact that the scouting id
     * column has data in it.
     *
     * @param row
     * @param pidIndex
     * @return
     */
    boolean containsDataToImport(Row row, int pidIndex) {
        Cell pidCell = row?.getCell(pidIndex)
        return pidCell && pidCell.cellType != HSSFCell.CELL_TYPE_BLANK;
    }

    /**
     * Actually performs the entire import, by:
     *
     * Looping sheet
     * @param importJob
     */
    void processImportJob(ImportJob importJob) {


        searchableService.stopMirroring()
        //This happens in a thread, so manual transaction and session boundaries are required
        Leader.withTransaction {
            try {
                Leader.withSession {Session session ->
                    def certificationMap = [:]
                    Certification.list().each {Certification certification ->
                        certificationMap[certification.externalId] = certification
                    }
                    for (ImportSheet currentSheet: importJob.sheetsToImport) {
                        importJob.currentSheet = currentSheet
                        currentSheet.importStatus = ImportStatus.Processing

                        int startRowNumber = currentSheet.headerRow.getRowNum()
                        int endRowNumber = currentSheet.workbookSheet.physicalNumberOfRows
                        for (int i = startRowNumber + 1; i < endRowNumber; i++) {
                            Row row = currentSheet.workbookSheet.getRow(i)
                            if (containsDataToImport(row, currentSheet.pidIndex)) {
                                ImportedRecord record = null
                                try {
                                    record = new ImportedRecord()
                                    currentSheet.headerIndex.each {
                                        int cellIndex = it.value
                                        String spreadsheetFieldName = it.key
                                        Cell cell = row.getCell(cellIndex)
                                        def dataClosure = dataTypeMap[spreadsheetFieldName]
                                        if (!dataClosure) {
                                            throw new IllegalStateException("Closure mapping missing for field ${spreadsheetFieldName}")
                                        }
                                        def spreadSheetData = dataClosure(cell)

                                        String importRecoredPropertyName = headerMap[spreadsheetFieldName]
                                        record.setProperty(importRecoredPropertyName, spreadSheetData)
                                    }

                                    if (!record.unitNumber) {
                                        //@todo this pains me to do this - wrestled with validation before giving up
                                        throw new IllegalStateException("Missing unit number")
                                    }

                                    //@todo Very expensive stuff - make it work, then optimize

                                    ScoutUnitType unitType = ScoutUnitType.valueOf(record.unitType)

                                    //Make sure unit exists
                                    ScoutGroup existingUnit = ScoutGroup.findByGroupIdentifierAndUnitType(record.unitNumber, unitType)
                                    if (!existingUnit) {
                                        throw new Exception("Unknown unit")

                                        //Add the unit.  These should all be pre-created, though
//                                    existingUnit = new ScoutGroup()
//                                    existingUnit.groupIdentifier = record.unitNumber
//                                    existingUnit.groupLabel = record.unitNumber
//                                    existingUnit.groupType = ScoutGroupType.Unit
//
//                                    existingUnit.unitType = unitType
//                                    existingUnit.save(failOnError: true)
                                    }

                                    LeaderPositionType position = leaderPositionTypeMap[record.position?.replaceAll("\\s", "")]
                                    if (position == null) {
                                        throw new Exception("No position code")
                                    }

                                    Leader leader = leaderService.findExactLeaderMatch(record.scoutingId, record.email, record.firstName,
                                            record.lastName, existingUnit)
                                    if (!leader) {
                                        leader = new Leader()
                                        leader.firstName = record.firstName
                                        leader.lastName = record.lastName
                                        leader.email = record.email

                                        leader.addToMyScoutingIds(myScoutingIdentifier: record.scoutingId)
                                        leader.save(flush:true, failOnError: true)


                                        existingUnit.addToLeaderGroups([leader: leader, position: position])

                                        processCertification(record,certDefinitionMap, certificationMap,leader,importJob);

                                        // leader specific
                                        if(position) {
                                            processCertification(record, positionTypeToSpecificMap.get(position), certificationMap, leader, importJob);
                                        }


                                    } else {
                                        leader.firstName = record.firstName ?: leader.firstName
                                        leader.lastName = record.lastName ?: leader.lastName
                                        leader.email = record.email ?: leader.email
                                        if (!leader.hasScoutingId(record.scoutingId)) {
                                            leader.addToMyScoutingIds(myScoutingIdentifier: record.scoutingId)
                                        }
                                        leader.save(flush:true, failOnError: true)


                                        if (record.unitNumber) {
                                            if (!existingUnit.leaderGroups?.collect {it.leader?.id}?.contains(leader.id)) {
                                                existingUnit.addToLeaderGroups([leader: leader, position: position])
                                            }
                                        }

                                        processCertification(record, certDefinitionMap, certificationMap,leader,importJob);

                                        // leader specific
                                        if(position) {
                                            processCertification(record, positionTypeToSpecificMap.get(position), certificationMap, leader, importJob);
                                        }
                                    }



                                    existingUnit.save(flush:true, failOnError: true)
                                    leader.refresh()
                                    existingUnit.refresh();
                                    trainingService.recalculatePctTrained(leader)

                                } catch (Exception e) {
                                    if (e instanceof ValidationException) {
                                        ValidationException ve = (ValidationException) e;
                                        ImportError importError = new ImportError(record: record)
                                        ve.errors.allErrors.each {   ObjectError err ->
                                            importError.addMessage(err)
                                        }
                                        currentSheet.errors << importError
                                    } else {
                                        e.printStackTrace()
                                        currentSheet.errors << new ImportError(record: record).addMessage(new DefaultMessageSourceResolvable(null, e.message))
                                    }

                                    currentSheet.totalErrors++

                                    //Make sure we don't attempt to save data in the session
                                    session.clear()
                                } finally {
                                    currentSheet.totalComplete++
                                }
                            }

                        }
                        currentSheet.totalComplete = currentSheet.totalToImport
                        currentSheet.importStatus = ImportStatus.Complete


                    }
                }

            } finally {
                scoutGroupService.reindex();
                searchableService.startMirroring()
                searchableService.reindexAll()

            }
        }


    }

    void processCertification(ImportedRecord record, Map definitionMap, Map certificationMap, Leader leader, ImportJob importJob) {
        definitionMap.each {entry ->
            Date trainingDate = record.getProperty(entry.key)
            if (trainingDate) {

                Certification certification = certificationMap[entry.value]
                if (!certification) {
                    throw new IllegalStateException("Certification ${entry.value} not found")
                }

                //Check to make sure there's not a newer training date on the record
                LeaderCertification existing = leader.certifications.find {return it.certification.id == certification.id}
                if (existing && trainingDate.after(existing.dateEarned)) {
                    existing.dateEarned = trainingDate
                    existing.save(failOnError: true)
                } else if (!existing) {
                    LeaderCertification leaderCertification = new LeaderCertification()
                    leaderCertification.leader = leader
                    leaderCertification.certification = certification
                    leaderCertification.dateEarned = trainingDate
                    leaderCertification.dateEntered = new Date()
                    leaderCertification.enteredType = LeaderCertificationEnteredType.Imported
                    leaderCertification.enteredBy = importJob.importedBy
                    leaderCertification.save(failOnError: true)
                    leader.addToCertifications(leaderCertification)
                    leader.save(failOnError: true)
                }
            }
        }
    }

}
