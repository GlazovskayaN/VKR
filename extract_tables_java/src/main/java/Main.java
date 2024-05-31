
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) throws IOException {
        ArrayList<String> linksForDocuments = new ArrayList<>();
       /* try(BufferedWriter writer = new BufferedWriter(new FileWriter("links.txt"))) {
            for (int page = 1; page <= 42; page++) {
                String url;
                if (page == 1) {
                    url = "https://minzdrav.gov.ru/documents?document_search[category_ids]=&document_search[issued_by]=&document_search[issued_from]=&document_search[issued_until]=&document_search[kind]=&document_search[number]=&document_search[order]=date_desc&document_search[q]=%D0%BE%D0%B1+%D1%83%D1%82%D0%B2%D0%B5%D1%80%D0%B6%D0%B4%D0%B5%D0%BD%D0%B8%D0%B8+%D1%81%D1%82%D0%B0%D0%BD%D0%B4%D0%B0%D1%80%D1%82+%D0%BF%D0%BE%D0%BC%D0%BE%D1%89%D0%B8&document_search[title_only]=true&page=&utf8=%E2%9C%93";
                } else {
                    url = "https://minzdrav.gov.ru/documents?document_search[category_ids]=&document_search[issued_by]=&document_search[issued_from]=&document_search[issued_until]=&document_search[kind]=&document_search[number]=&document_search[order]=date_desc&document_search[q]=%D0%BE%D0%B1+%D1%83%D1%82%D0%B2%D0%B5%D1%80%D0%B6%D0%B4%D0%B5%D0%BD%D0%B8%D0%B8+%D1%81%D1%82%D0%B0%D0%BD%D0%B4%D0%B0%D1%80%D1%82+%D0%BF%D0%BE%D0%BC%D0%BE%D1%89%D0%B8&document_search[title_only]=true&page=" + page + "&utf8=%E2%9C%93";
                }
                Document doc = Jsoup.connect(url).get();

                Elements documentIndexes = doc.select("documents_index");
                for (Element documentIndex : documentIndexes) {
                    Elements links = documentIndex.select("div.document__content h4.media-heading a");

                    for (Element link : links) {
                        String documentUrl = link.absUrl("href");
                        Document docForPDF = Jsoup.connect(documentUrl).get();
                        Element documentDownloadPdf = docForPDF.select("div.document_title a").first();
                        if (documentDownloadPdf != null) {
                            String documentUrlPDF = documentDownloadPdf.absUrl("href");
                            writer.write(documentUrlPDF);
                            writer.newLine();
                        }

                    }
                }
                Thread.sleep(625);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try (BufferedReader reader = new BufferedReader(new FileReader("links.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                linksForDocuments.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int f =0;
       for (int j = 0; j < linksForDocuments.size() ; j++) {
          String documentUrlPDF = linksForDocuments.get(j);
            URL url = new URL(documentUrlPDF);
            System.out.println(url);
            InputStream in = url.openStream();
            Files.copy(in, Paths.get("File.pdf"), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            String pdfFilePath = "File.pdf";
            try {
                PDDocument document = PDDocument.load(new File(pdfFilePath));
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String text = pdfStripper.getText(document);
                if(text.contains("■")){
                    text = "";
                }
                document.close();
                List<String> namesOfDiseases = new ParsePdf().getNameOfDisease(text);
                System.out.println(namesOfDiseases);
                List<String> infFromTable = new ParsePdf().preprocessingTextForTableMedicines(text);
                System.out.println(infFromTable);
                for(String inf: infFromTable){
                    if((inf.contains("!"))||(inf.contains("|"))||(inf.contains("'"))){
                        infFromTable.clear();
                        break;
                    }
                }
                Configuration configuration = new Configuration();
                configuration.setProperty("hibernate.connection.driver_class", "");
                configuration.setProperty("hibernate.connection.url", "");
                configuration.setProperty("hibernate.connection.username", "");
                configuration.setProperty("hibernate.connection.password", "");
                configuration.addAnnotatedClass(Diseases.class);
                configuration.addAnnotatedClass(Medications.class);
                configuration.addAnnotatedClass(MedicationTypes.class);
                configuration.addAnnotatedClass(Groups.class);
                configuration.addAnnotatedClass(MedicationDetails.class);
                configuration.addAnnotatedClass(Solutions.class);
                configuration.addAnnotatedClass(MedicineGroups.class);
                try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
                    try (Session session = sessionFactory.openSession()) {
                        if (!namesOfDiseases.isEmpty()) {
                            if (!infFromTable.isEmpty()) {
                                f++;
                                Transaction transaction = session.beginTransaction();
                            Map<String, String> diseaseMap = new HashMap<>();
                            for (String nameDisease : namesOfDiseases) {
                                String codeOfDisease = nameDisease.split("\\s+")[0];
                                nameDisease = nameDisease.replaceAll(Pattern.quote(codeOfDisease) + "\\s+", "");
                                if(codeOfDisease.contains("*")){
                                    codeOfDisease= codeOfDisease.replaceAll("\\*","").replaceAll("\\)","");
                                }
                                if (session.createQuery("FROM Diseases d WHERE d.diseaseName =: name AND d.diseaseCode =: code",Diseases.class).setParameter("name", nameDisease).setParameter("code", codeOfDisease).uniqueResult()==null) {
                                    Diseases diseases1 = new Diseases();
                                    diseases1.setDiseaseName(nameDisease);
                                    diseases1.setDiseaseCode(codeOfDisease);
                                    session.save(diseases1);
                                }
                                diseaseMap.put(codeOfDisease, nameDisease);
                            }
                            Map<String, String> medicationTypeMap = new HashMap<>();
                            Map<String, List<List<String>>> medicationsMap = new HashMap<>();


                                String medicationTypeName = "";
                                String codeOfMedication = "";
                                String probability = "";
                                for (String str : infFromTable) {
                                    str = str.trim();
                                    codeOfMedication = str.split("\\s+")[0];
                                    str = str.replaceFirst(Pattern.quote(codeOfMedication) + "\\s+", "");
                                    Pattern pattern1 = Pattern.compile("[\\s\\S]*?(?=(\\s1\\s|\\d+\\,\\d+))");
                                    Matcher matcher1 = pattern1.matcher(str);
                                    if (matcher1.find()) {
                                        medicationTypeName = matcher1.group(0).trim();
                                    }
                                    else{
                                        Pattern pattern2 = Pattern.compile("([A-Z]|[А-Я])[\\s\\S]*?(?=(\\s+([A-Z]|[А-Я])([a-z]|[а-я])([a-z]|[а-я])([a-z]|[а-я])*\\s+))");
                                        Matcher matcher2 =pattern2.matcher(str);
                                        if(matcher2.find()){
                                            medicationTypeName = matcher2.group().trim();
                                        }
                                    }
                                    str = str.replaceFirst(Pattern.quote(medicationTypeName) + "\\s+", "");
                                    if(str.split("\\s+")[0].contains("0,")||str.split("\\s+")[0].contains("1")||str.split("\\s+")[0].contains("0.")){
                                    probability = str.split("\\s+")[0];}
                                    probability = probability.replace(",", ".");
                                    if(probability!=""){
                                    str = str.replaceFirst(probability + "\\s+", "");}
                                    str = str.trim();
                                    medicationTypeMap.put(codeOfMedication, medicationTypeName);
                                    if (session.createQuery("FROM MedicationTypes mt WHERE mt.medicationTypeName =:name AND mt.medicationTypeCode=:code", MedicationTypes.class).setParameter("name", medicationTypeName).setParameter("code", codeOfMedication).uniqueResult() == null) {
                                      MedicationTypes  medicationTypes1 = new MedicationTypes();
                                        medicationTypes1.setMedicationTypeName(medicationTypeName);
                                        medicationTypes1.setMedicationTypeCode(codeOfMedication);
                                        session.save(medicationTypes1);
                                    }
                                    List<List<String>> medicationsElements = new ArrayList<>();
                                    while (!str.isEmpty()) {
                                        String trimmedInput = str.trim();
                                        int endIndex = trimmedInput.length() - 1;
                                        StringBuilder strWithNum1 = new StringBuilder();

                                        while (endIndex >= 0 && !Character.isLetter(trimmedInput.charAt(endIndex))) {
                                            strWithNum1.insert(0, trimmedInput.charAt(endIndex));
                                            endIndex--;
                                        }
                                        String strWithNum = strWithNum1.toString();
                                        Pattern pat = Pattern.compile("\\d+\\s+\\d+,*");
                                        Matcher mat = pat.matcher(strWithNum);
                                        while (mat.find()) {
                                            String st = mat.group().trim();
                                            String[] s1 = st.split("\\s+");
                                            if (s1[1].startsWith("0") && !s1[1].contains(",")) {
                                                st = st.replaceAll("\\s+", "");
                                            }
                                            strWithNum = strWithNum.replaceAll(Pattern.quote(mat.group()), st);
                                        }
                                        str = str.replaceAll(Pattern.quote(strWithNum1.toString()), strWithNum);
                                        Pattern pat1 = Pattern.compile("\\s+\\d+((,|\\.)\\d+)*");
                                        Matcher mat1 = pat1.matcher(strWithNum);
                                        List<String> strNum = new ArrayList<>();
                                        String strWithNum2 = strWithNum;
                                        while(mat1.find()){
                                            strNum.add(mat1.group());
                                        }
                                        if(strNum.size()>2){
                                            String []str1  = strWithNum.split("\\s+");
                                            int l = strWithNum.lastIndexOf(str1[str1.length-1]);
                                            strWithNum =   strWithNum.substring(0,l).trim();
                                        }
                                        str = str.replaceAll(Pattern.quote(strWithNum2), strWithNum);
                                        List<String> elements = new ArrayList<>();
                                        Pattern pattern2 = Pattern.compile("\\s+\\d+((,|\\.)\\d+)*(\\s*\\+\\s*\\d+((,|\\.)\\d+)*)*\\s*$");
                                        Matcher matcher2 = pattern2.matcher(str);

                                        if (matcher2.find()) {
                                            elements.add(matcher2.group().trim());
                                            int lastMatchStart = matcher2.start();
                                            str = str.substring(0, lastMatchStart);
                                        }
                                        matcher2 = pattern2.matcher(str);
                                        if (matcher2.find()) {
                                            elements.add(matcher2.group().trim());
                                            int lastMatchStart = matcher2.start();
                                            str = str.substring(0, lastMatchStart);
                                        }
                                        String[] strings = str.split("\\s+");
                                        elements.add(strings[strings.length - 1].trim());
                                        String[] wordsWithoutLast = Arrays.copyOfRange(strings, 0, strings.length - 1);
                                        str = String.join(" ", wordsWithoutLast);
                                        strings = str.split("\\s+\\d+((,|\\.)\\d+)*(\\s*\\+\\s*\\d+((,|\\.)\\d+)*)*\\s+\\d+((,|\\.)\\d+)*(\\s*\\+\\s*\\d+((,|\\.)\\d+)*)*\\s+");
                                        elements.add(strings[strings.length - 1].trim());
                                        Pattern pattern3 = Pattern.compile("\\s+\\d+(\\.|,)*\\s+");
                                        if(elements.size()>3){
                                        Matcher matcher3 = pattern3.matcher(elements.get(3));
                                        if(matcher3.find()){
                                            elements.set(3,elements.get(3).replaceAll(Pattern.quote(matcher3.group()),""));
                                        }}
                                        int lastIndex = str.lastIndexOf(strings[strings.length - 1]);
                                        if (lastIndex != -1) {
                                            str = str.substring(0, lastIndex);
                                        }
                                        str = str.trim();
                                        elements.add(probability);

                                        if(elements.size()>4){
                                            medicationsElements.add(elements);
                                        if ((elements.get(3).contains("+"))) {
                                            Groups groups1 = new Groups();
                                            groups1.setGroupName(elements.get(3).trim());
                                            String[] unionList = {};
                                            String union = elements.get(2);
                                            if (union.contains("+")) {
                                                unionList = union.split("\\+");
                                            }
                                            if (elements.get(0).contains("+")) {
                                                Groups groupsResult = session.createQuery("FROM Groups g " +
                                                        "WHERE g.groupname = :name", Groups.class).setParameter("name", groups1.getGroupName()).uniqueResult();
                                                if (groupsResult == null) {
                                                    session.save(groups1);
                                                }
                                                String[] medicationslist1 = elements.get(3).split("\\+");
                                                for (int i = 0; i < medicationslist1.length; i++) {
                                                    Medications medications1 = new Medications();
                                                    session.clear();
                                                    medications1.setMedicationName(medicationslist1[i].trim().replaceAll("\\[","").replaceAll("]","").replaceAll("\\{","").replaceAll("}",""));
                                                    if (unionList.length == medicationslist1.length) {
                                                        medications1.setUnits(unionList[i].trim());
                                                    } else {
                                                        medications1.setUnits(union);
                                                    }
                                                    Medications medicationsResult = session.createQuery("FROM Medications m " +
                                                            "WHERE m.medicationName = :name", Medications.class).setParameter("name", medications1.getMedicationName()).uniqueResult();
                                                    if (medicationsResult == null) {
                                                        session.save(medications1);
                                                    }
                                                }
                                            } else {
                                                groups1.setUnits(union);
                                                Groups groupsResult = session.createQuery("FROM Groups g " +
                                                        "WHERE g.groupname = :name", Groups.class).setParameter("name", groups1.getGroupName()).uniqueResult();
                                                if (groupsResult == null) {
                                                    session.save(groups1);
                                                } else {
                                                    groups1 = session.createQuery("FROM Groups g WHERE g.groupname = :groupname", Groups.class).setParameter("groupname", groups1.getGroupName()).uniqueResult();
                                                }
                                                String sol = "";
                                                if (elements.get(3).contains("[") || elements.get(3).contains("{")) {
                                                    Pattern p = Pattern.compile("(\\[|\\{)(.*?)(\\]|\\})");
                                                    Matcher m = p.matcher(elements.get(3));

                                                    if (m.find()) {
                                                        sol = m.group();
                                                    }
                                                } else {
                                                    sol = elements.get(3);
                                                }
                                                String[] med = sol.split("\\+");
                                                for (int i = 0; i < med.length; i++) {
                                                    Medications medications1 = new Medications();
                                                    session.clear();
                                                    medications1.setMedicationName(med[i].trim().replaceAll("\\[","").replaceAll("]","").replaceAll("\\{","").replaceAll("}",""));
                                                    medications1.setUnits(union);
                                                    Medications medicationsResult = session.createQuery("FROM Medications m " +
                                                            "WHERE m.medicationName = :name", Medications.class).setParameter("name", medications1.getMedicationName()).uniqueResult();
                                                    if (medicationsResult == null) {
                                                        session.save(medications1);
                                                    }
                                                }
                                                Solutions solutions1 = new Solutions();
                                                solutions1.setGroup(groups1);
                                                String daily = elements.get(1);
                                                String course = elements.get(0);
                                                if (daily.contains(",")) {
                                                    daily = daily.replace(",", ".");
                                                }
                                                if (course.contains(",")) {
                                                    course = course.replace(",", ".");
                                                }
                                                solutions1.setDailyDose(Double.parseDouble(daily));
                                                solutions1.setCourseDose(Double.parseDouble(course));
                                                if (session.createQuery("FROM Solutions s WHERE s.group =:g AND s.courseDose=:cd AND s.dailyDose =: dd", Solutions.class).setParameter("g", groups1).setParameter("cd", Double.parseDouble(course)).setParameter("dd", Double.parseDouble(daily)).uniqueResult() == null) {
                                                    session.save(solutions1);
                                                }
                                            }

                                        } else {
                                            Medications medications1 = new Medications();
                                            medications1.setMedicationName(elements.get(3).replaceAll("\\[","").replaceAll("]","").replaceAll("\\{","").replaceAll("}",""));
                                            medications1.setUnits(elements.get(2));
                                            Medications medicationsResult = session.createQuery("FROM Medications m " +
                                                    "WHERE m.medicationName = :name", Medications.class).setParameter("name", medications1.getMedicationName()).uniqueResult();
                                            if (medicationsResult == null) {
                                                session.save(medications1);
                                            }
                                        }


                                    medicationsMap.put(codeOfMedication, medicationsElements); }

                                }}

                                for (Map.Entry<String, String> entry1 : diseaseMap.entrySet()) {
                                    Diseases diseases2 = session.createQuery("FROM Diseases d WHERE d.diseaseCode = :code AND d.diseaseName = :name", Diseases.class).setParameter("code", entry1.getKey()).setParameter("name", entry1.getValue()).uniqueResult();
                                    for (Map.Entry<String, String> entry2 : medicationTypeMap.entrySet()) {
                                        MedicationTypes medicationTypes2 = session.createQuery("FROM MedicationTypes mt WHERE mt.medicationTypeCode = :code AND mt.medicationTypeName = :name", MedicationTypes.class).setParameter("code", entry2.getKey()).setParameter("name", entry2.getValue()).uniqueResult();
                                        List<List<String>> medicationsForData1 = medicationsMap.get(medicationTypes2.getMedicationTypeCode());
                                        if(medicationsForData1!=null&&medicationsForData1.size()>0){
                                        for (List<String> string : medicationsForData1) {
                                            List<String> medicationsForData = string;
                                            if (medicationsForData.get(3).contains("+")) {
                                                Groups groups = session.createQuery("FROM Groups g WHERE g.groupname = : name", Groups.class).setParameter("name", medicationsForData.get(3)).uniqueResult();
                                                if (medicationsForData.get(0).contains("+")) {
                                                    String[] med = medicationsForData.get(3).split("\\+");
                                                    String[] dailyDose = medicationsForData.get(1).split("\\+");
                                                    String[] courseDose = medicationsForData.get(0).split("\\+");
                                                    for (int i = 0; i < med.length; i++) {
                                                        if (dailyDose[i].contains(",")) {
                                                            dailyDose[i] = dailyDose[i].replaceAll(",", ".");
                                                        }
                                                        if (courseDose[i].contains(",")) {
                                                            courseDose[i] = courseDose[i].replaceAll(",", ".");
                                                        }
                                                        Medications medications = session.createQuery("FROM Medications m WHERE m.medicationName=:name", Medications.class).setParameter("name", med[i].trim().replaceAll("\\[","").replaceAll("]","").replaceAll("\\{","").replaceAll("}","")).uniqueResult();
                                                        MedicationDetails medicationDetails = new MedicationDetails();
                                                        medicationDetails.setDeseases(diseases2);
                                                        medicationDetails.setMedicationTypes(medicationTypes2);
                                                        medicationDetails.setMedication(medications);
                                                        medicationDetails.setCourseDose(Double.parseDouble(courseDose[i].trim()));
                                                        medicationDetails.setDailyDose(Double.parseDouble(dailyDose[i].trim()));
                                                        medicationDetails.setGroup(groups);
                                                        if(medicationsForData.get(4)!=""){
                                                        medicationDetails.setProbability(Double.parseDouble(medicationsForData.get(4).replaceAll(",", ".").trim()));}
                                                        if (session.createQuery("FROM MedicationDetails md WHERE md.deseases =:d AND md.medication =:m AND md.medicationTypes =:mt", MedicationDetails.class).setParameter("d", diseases2).setParameter("m", medications).setParameter("mt", medicationTypes2).uniqueResult() == null) {
                                                            session.save(medicationDetails);
                                                        }
                                                    }
                                                } else {
                                                    String sol = "";
                                                    if (medicationsForData.get(3).contains("[") || medicationsForData.get(3).contains("{")) {
                                                        Pattern p = Pattern.compile("(\\[|\\{)(.*?)(\\]|\\})");
                                                        Matcher m = p.matcher(medicationsForData.get(3));
                                                        if (m.find()) {
                                                            sol = m.group();
                                                        }
                                                    } else {
                                                        sol = medicationsForData.get(3);
                                                    }
                                                    String[] med = sol.split("\\+");
                                                    Solutions solutions = session.createQuery("FROM Solutions s WHERE s.group=:g AND s.courseDose =: cd AND s.dailyDose =:dd", Solutions.class).setParameter("g", groups)
                                                            .setParameter("cd",Double.parseDouble(medicationsForData.get(0).replaceAll(",",".").trim()))
                                                            .setParameter("dd",Double.parseDouble(medicationsForData.get(1).replaceAll(",",".").trim())).uniqueResult();
                                                    for (int i = 0; i < med.length; i++) {
                                                        Medications medications = session.createQuery("FROM Medications m WHERE m.medicationName=:name", Medications.class).setParameter("name", med[i].trim().replaceAll("\\[","").replaceAll("]","").replaceAll("\\{","").replaceAll("}","")).uniqueResult();
                                                        MedicationDetails medicationDetails = new MedicationDetails();
                                                        medicationDetails.setMedication(medications);
                                                        medicationDetails.setDeseases(diseases2);
                                                        medicationDetails.setMedicationTypes(medicationTypes2);
                                                        medicationDetails.setSolutions(solutions);
                                                        medicationDetails.setGroup(groups);
                                                        if(medicationsForData.get(4)!=""){
                                                        medicationDetails.setProbability(Double.parseDouble(medicationsForData.get(4).replaceAll(",", ".").trim()));}
                                                        if (session.createQuery("FROM MedicationDetails md WHERE md.deseases =:d AND md.medication =:m AND md.medicationTypes =:mt", MedicationDetails.class).setParameter("d", diseases2).setParameter("m", medications).setParameter("mt", medicationTypes2).uniqueResult() == null) {
                                                            session.save(medicationDetails);
                                                        }
                                                    }
                                                }
                                            } else {
                                                Medications medications = session.createQuery("FROM Medications m WHERE m.medicationName=:name", Medications.class).setParameter("name", medicationsForData.get(3).trim().replaceAll("\\[","").replaceAll("]","").replaceAll("\\{","").replaceAll("}","")).uniqueResult();
                                                MedicationDetails medicationDetails = new MedicationDetails();
                                                medicationDetails.setMedication(medications);
                                                medicationDetails.setDeseases(diseases2);
                                                medicationDetails.setMedicationTypes(medicationTypes2);
                                                if(medicationsForData.get(4)!=""){
                                                medicationDetails.setProbability(Double.parseDouble(medicationsForData.get(4).replaceAll(",", ".").trim()));}
                                                medicationDetails.setDailyDose(Double.parseDouble(medicationsForData.get(1).replaceAll(",", ".").trim()));
                                                medicationDetails.setCourseDose(Double.parseDouble(medicationsForData.get(0).replaceAll(",", ".").trim()));
                                                if (session.createQuery("FROM MedicationDetails md WHERE md.deseases =:d AND md.medication =:m AND md.medicationTypes =:mt", MedicationDetails.class).setParameter("d", diseases2).setParameter("m", medications).setParameter("mt", medicationTypes2).uniqueResult() == null) {
                                                    session.save(medicationDetails);
                                                }
                                            }}

                                        }
                                    }
                                }



                        transaction.commit(); } }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
           System.out.println(f); }
    }}





