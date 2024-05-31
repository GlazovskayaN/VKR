

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "diseases", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "unique_diseasecode", columnNames = {"diseasecode"}),
        @UniqueConstraint(name = "unique_diseasename", columnNames = {"diseasename"})
})
 class Diseases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diseaseid")
    private int diseaseId;

    @Column(name = "diseasename", nullable = false)
    private String diseaseName;

    @Column(name = "diseasecode", nullable = false)
    private String diseaseCode;

    public int getDiseaseId() {
        return diseaseId;
    }

    public void setDiseaseId(int diseaseId) {
        this.diseaseId = diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getDiseaseCode() {
        return diseaseCode;
    }

    public void setDiseaseCode(String diseaseCode) {
        this.diseaseCode = diseaseCode;
    }
}

@Entity
@Table(name = "medications", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "medications_medicationname_key", columnNames = {"medicationname"})
})
class Medications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicationid")
    private int medicationId;

    @Column(name = "medicationname", nullable = false)
    private String medicationName;

    @Column(name = "units")
    private String units;

    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
@Entity
@Table(name = "medicationtypes", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "medicationtypes_medicationtypename_medicationtypecode_key", columnNames = {"medicationtypename", "medicationtypecode"})
})
class MedicationTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicationtypeid")
    private int medicationTypeId;

    @Column(name = "medicationtypename", nullable = false)
    private String medicationTypeName;

    @Column(name = "medicationtypecode", nullable = false)
    private String medicationTypeCode;


    public int getMedicationTypeId() {
        return medicationTypeId;
    }

    public void setMedicationTypeId(int medicationTypeId) {
        this.medicationTypeId = medicationTypeId;
    }

    public String getMedicationTypeName() {
        return medicationTypeName;
    }

    public void setMedicationTypeName(String medicationTypeName) {
        this.medicationTypeName = medicationTypeName;
    }

    public String getMedicationTypeCode() {
        return medicationTypeCode;
    }

    public void setMedicationTypeCode(String medicationTypeCode) {
        this.medicationTypeCode = medicationTypeCode;
    }
}
@Entity
@Table(name = "groups", schema = "public")
class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "groupname")
    private String groupname;
@Column(name = "units")
 private String units;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupname;
    }

    public void setGroupName(String groupname) {
        this.groupname = groupname;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
@Embeddable
class MedicineGroupId implements Serializable {

    @Column(name = "medicationid", nullable = false)
    private int medicationId;

    @Column(name = "groupid", nullable = false)
    private int groupId;

    public MedicineGroupId() {
    }

    public MedicineGroupId(int medicationId, int groupId) {
        this.medicationId = medicationId;
        this.groupId = groupId;
    }


    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}

@Entity
@Table(name = "medicinegroups", schema = "public")
class MedicineGroups {

    @EmbeddedId
    private MedicineGroupId id;

    @ManyToOne
    @JoinColumn(name = "groupid", insertable = false, updatable = false)
    private Groups group;

    @ManyToOne
    @JoinColumn(name = "medicationid", insertable = false, updatable = false)
    private Medications medication;


    public MedicineGroupId getId() {
        return id;
    }

    public void setId(MedicineGroupId id) {
        this.id = id;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }

    public Medications getMedication() {
        return medication;
    }

    public void setMedication(Medications medication) {
        this.medication = medication;
    }
}



@Entity
@Table(name = "solutions", schema = "public")
class Solutions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "coursedose")
    private double courseDose;

    @Column(name = "dailydose")
    private double dailyDose;

    @ManyToOne
    @JoinColumn(name = "groupsid", nullable = false)
    private Groups group;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCourseDose() {
        return courseDose;
    }

    public void setCourseDose(double courseDose) {
        this.courseDose = courseDose;
    }

    public double getDailyDose() {
        return dailyDose;
    }

    public void setDailyDose(double dailyDose) {
        this.dailyDose = dailyDose;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }
}
@Entity
@Table(name = "medicationdetails", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "medicationdetails_un", columnNames = {"medicationid","deseaseid","medicationtypeid" })
})
class MedicationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "medicationid",nullable = false)
    private Medications medication;
    @ManyToOne
    @JoinColumn(name = "medicationtypeid",nullable = false)
    private MedicationTypes medicationTypes;
    @ManyToOne
    @JoinColumn(name = "deseaseid",nullable = false)
    private Diseases deseases;
    @ManyToOne
    @JoinColumn(name = "solutionid")
    private Solutions solutions;
    @Column(name = "dailydose")
    private double dailyDose;
    @Column(name = "coursedose")
    private double courseDose;
    @Column(name = "probability")
    private double probability;

    @ManyToOne
    @JoinColumn(name = "groupid")
    private Groups group;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Medications getMedication() {
        return medication;
    }

    public void setMedication(Medications medication) {
        this.medication = medication;
    }

    public MedicationTypes getMedicationTypes() {
        return medicationTypes;
    }

    public void setMedicationTypes(MedicationTypes medicationTypes) {
        this.medicationTypes = medicationTypes;
    }

    public double getDailyDose() {
        return dailyDose;
    }

    public void setDailyDose(double dailyDose) {
        this.dailyDose = dailyDose;
    }

    public double getCourseDose() {
        return courseDose;
    }

    public void setCourseDose(double courseDose) {
        this.courseDose = courseDose;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public Diseases getDeseases() {
        return deseases;
    }

    public void setDeseases(Diseases deseases) {
        this.deseases = deseases;
    }

    public Groups getGroup() {
        return group;
    }

    public Solutions getSolutions() {
        return solutions;
    }

    public void setSolutions(Solutions solutions) {
        this.solutions = solutions;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }
}


