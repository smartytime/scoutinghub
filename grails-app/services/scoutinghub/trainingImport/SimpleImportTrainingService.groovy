package scoutinghub.trainingImport

import org.apache.commons.httpclient.util.DateParseException
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import scoutinghub.*

/**
 * Created by IntelliJ IDEA.
 * User: ericm
 * Date: 8/10/11
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */
class SimpleImportTrainingService {

    SessionFactory sessionFactory

    TrainingService trainingService

    void processSimpleImportJob(SimpleImportJob simpleImportJob) {
        //Scan column headers for certification mappings

        String[] line
        int saveCount = 1
        Transaction transaction = sessionFactory.currentSession.beginTransaction()
        transaction.begin()
        while ((line = simpleImportJob.csvReader.readNext()) != null) {
            simpleImportJob.totalCompleted++
            String personId = line[0]

            if (personId && personId != "person_id") {
                try {
                    Leader foundLeader = MyScoutingId.findByMyScoutingIdentifier(personId)?.leader

                    if (!foundLeader) {
                        //Create a new leader record with the data from
                        foundLeader = new Leader()
                        foundLeader.firstName = line[2]
                        foundLeader.lastName = line[4]
                        foundLeader.email = line[7]

                        foundLeader.addToMyScoutingIds([myScoutingIdentifier: personId])
                        foundLeader.save()
                    }
                    if (foundLeader.hasErrors()) {
                        println foundLeader.errors
                        foundLeader.discard()

                    } else {
                        if (!foundLeader.email) {
                            String emailFromCsv = line[simpleImportJob.emailColumn]
                            if (emailFromCsv) {
                                foundLeader.email = emailFromCsv
                                if (!foundLeader.validate()) {
                                    println "Invalid email: ${emailFromCsv}"
                                    foundLeader.email = null
                                }
                            }
                        }
                        simpleImportJob.columnIndexToCertificationMap.each {entry ->
                            int cellId = entry.key
                            Certification certification = entry.value

                            String dateValue = line[cellId]
                            if (dateValue && dateValue.length() > 0) {

                                Date trainingDate
                                try {
                                    trainingDate = Date.parse("MM/dd/yy", dateValue)
                                } catch (DateParseException de) {
                                    println "Failed to parse date: ${dateValue}"
                                }

                                if (trainingDate) {
                                    //Check to make sure there's not a newer training dateValue on the record
                                    LeaderCertification existing = foundLeader.certifications.find {
                                        return it.certification.id == certification.id
                                    }


                                    if (existing && trainingDate.after(existing.dateEarned)) {
                                        existing.dateEarned = trainingDate
                                        existing.save(failOnError: true)
                                        trainingService.recalculatePctTrained(foundLeader)


                                    } else if (!existing) {
                                        LeaderCertification leaderCertification = new LeaderCertification()
                                        leaderCertification.leader = foundLeader
                                        leaderCertification.certification = certification
                                        leaderCertification.dateEarned = trainingDate
                                        leaderCertification.dateEntered = new Date()
                                        leaderCertification.enteredType = LeaderCertificationEnteredType.Imported
                                        leaderCertification.enteredBy = simpleImportJob.importedBy
                                        leaderCertification.save(failOnError: true)
                                        foundLeader.addToCertifications(leaderCertification)
                                        foundLeader.save(failOnError: true)
                                        trainingService.recalculatePctTrained(foundLeader)
                                    }
                                }
                            }
                        }
                        saveCount++
                        if (saveCount % 100 == 0) {
                            saveCount = 1
                            println "Processed 100 records - flushing"
                            transaction.commit()
                            sessionFactory.currentSession.flush()
                            sessionFactory.currentSession.clear()
                            transaction = sessionFactory.currentSession.beginTransaction()
                            transaction.begin()
                        }

                    }
                } catch (Exception e) {
                    println "Error importing: ${e}"
                }
            }

        }

        transaction.commit()

        sessionFactory.currentSession.flush()
        sessionFactory.currentSession.clear()
    }

    def setupSimpleImportJob(SimpleImportJob simpleImportJob) {
        //Scan column headers for certification mappings
        def headerRow = simpleImportJob.csvReader.readNext();

        if (!headerRow) {
            throw new Exception("simpleImport.noHeaderRow")
        }
        int columnIndex = 0
        headerRow.each {String cell ->
            if (cell?.equalsIgnoreCase("Net Address")) {
                simpleImportJob.emailColumn = columnIndex
            } else {

                Certification foundCertification = CertificationCode.findByCode(cell)?.certification
                if (foundCertification) {
                    log.info "Mapped ${cell} to ${foundCertification.name}"
                    simpleImportJob.columnIndexToCertificationMap[columnIndex] = foundCertification
                }
            }
            columnIndex++
        }

        //Calculate total number of rows to process
        simpleImportJob.totalToProcess = 36000
    }
}
