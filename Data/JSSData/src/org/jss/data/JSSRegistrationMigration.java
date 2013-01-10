/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jss.data;

import au.com.bytecode.opencsv.CSVReader;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.jss.restClient.OpenMRSDataClient;
import org.jss.restClient.OpenMRSRestClient;
import org.jss.restClient.SubCenterLocation;

/**
 *
 * @author manorlev-tov
 */
public class JSSRegistrationMigration {

    private File mdbFile;
    private OpenMRSDataClient client;
    private Map<String, String> attributes = new HashMap<String, String>();
    private Map<Integer, String> casteIDs;
    private Map<Integer, String[]> locationIDs;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
            throws Exception {
        JSSRegistrationMigration j = new JSSRegistrationMigration(new OpenMRSDataClient(new OpenMRSRestClient("http://localhost:8080/openmrs", "admin", "Hello123"), true),
                new File("/home/raxajssemr/Desktop/Hospital/Data/JSSData2k.mdb"));
        j.migrateData();
    }

    public JSSRegistrationMigration(OpenMRSDataClient client, File mdbFile) throws IOException, JSONException {
        this.mdbFile = mdbFile;
        this.client = client;
        populateAttributes();
    }

    private void populateAttributes() {
        attributes.put("FHNAME", "FHFirstName");
        attributes.put("EDUCATION", "Education");
        attributes.put("OCCUPATION", "Occupation");
    }

    public void migrateData()
            throws IOException, JSONException {
        String bahUuid = client.getUuidByDisplay("location", "", "JSS Ba");
        String ganUuid = client.getUuidByDisplay("location", "", "JSS Gan");
        String semUuid = client.getUuidByDisplay("location", "", "JSS Sem");
        String shiUuid = client.getUuidByDisplay("location", "", "JSS Shiv");
        String tehsilUuid = client.getUuidByDisplayNoSearch("personattributetype", "Tehsil");
        String oldPatientUuid = client.getUuidByDisplayNoSearch("personattributetype", "Old Patient");
        String fatherUuid = client.getUuidByDisplayNoSearch("personattributetype", "Primary Relative");
        String casteUuid = client.getUuidByDisplayNoSearch("personattributetype", "Caste");
        String educationUuid = client.getUuidByDisplayNoSearch("personattributetype", "Education");
        String occupationUuid = client.getUuidByDisplayNoSearch("personattributetype", "Occupation");
        String registryTypeUuid = client.getUuidByDisplayNoSearch("personattributetype", "Registry Type");
        String weightUuid = client.getUuidByDisplay("concept", "Weight", "WEIGHT (KG)");
        String heightUuid = client.getUuidByDisplay("concept", "Height", "HEIGHT (CM)");
        String bmiUuid = client.getUuidByDisplay("concept", "bmi", "BODY MASS INDEX");
        String regEncounterTypeUuid = client.getUuidByDisplay("encountertype", "registration", "Registration encounter");
        
        int xx=0;
        Boolean goodToGo = false;
        Table registrationData = Database.open(mdbFile).getTable("RegistrationMaster");
        for (Map<String, Object> row : registrationData) {
            String regEntry = (String) row.get("REG_NO");
            try{
            if(regEntry.indexOf("/")!=-1){
            String registrationNumber = regEntry.substring(0, regEntry.indexOf("/"));
            Date birthDate = (Date) row.get("P_DOB");
            if(((String)registrationNumber).equals("105585")){
                goodToGo = true;
//                try{
//                birthDate = new SimpleDateFormat("yyMMddHHmmss").parse("540606041500");
//                }catch(Exception e){System.out.println(e.getMessage());}
            }
            if(goodToGo){
            String regLocation;
            String RegEntryLocation = regEntry.substring(regEntry.lastIndexOf("/") + 1);
            String regLocString = "GAN";
            if (RegEntryLocation.startsWith("B") || RegEntryLocation.startsWith("b")) {
                regLocation = bahUuid;
                regLocString = "BAH";
            } else if (RegEntryLocation.startsWith("SH") || RegEntryLocation.startsWith("Sh") || RegEntryLocation.startsWith("sh") ||
                    RegEntryLocation.startsWith("SI") || RegEntryLocation.startsWith("Si") || RegEntryLocation.startsWith("si")) {
                regLocation = shiUuid;
                regLocString = "SHI";
            } else if (RegEntryLocation.startsWith("SE") || RegEntryLocation.startsWith("Se") || RegEntryLocation.startsWith("se")) {
                regLocation = semUuid;
                regLocString = "SEM";
            } else {
                regLocation = ganUuid;
                regLocString = "GAN";
            }
            String firstName = (String)row.get("FNAME");
            String lastName = (String)row.get("LNAME");
            String fatherName = (String)row.get("FHNAME");
            if(firstName.equals("")){
                System.out.println("no first name");
               firstName = "undefined";
            }
            if(lastName.equals("")){
                lastName = "undefined";
                String[] names = firstName.split(" ");
                if(names.length>1){
                    lastName = names[names.length-1];
                    firstName="";
                    for(int y=0; y<names.length-1; y++){
                        firstName = firstName+names[y]+" ";
                    }
                    firstName=firstName.trim();
                }
            }
            String personUuid = client.addPerson(firstName, lastName, Util.pad(registrationNumber), regLocation, (String) row.get("P_SEX"), birthDate,"Chhattisgarh",(String) row.get("CITY"), (String) row.get("Tahsil"), (String) row.get("VILLAGE"));
            String patientUuid = client.addPatient(personUuid, client.newPatientIdentifier(regLocString), regLocation);

            String tehsil = (String)row.get("Tahsil");
            String education = (String)row.get("EDUCATION");
            String occupation = (String)row.get("OCCUPATION");

            if(tehsil!=null && !tehsil.equals("")){
                client.addPersonAttribute(patientUuid, "Tehsil", (String) row.get("Tahsil"), tehsilUuid);
            }
            if(fatherName!=null && !fatherName.equals("")){
                client.addPersonAttribute(patientUuid, "Primary Relative", fatherName, fatherUuid);
            }
            client.addPersonAttribute(patientUuid, "Old Patient Identification Number", Util.pad(registrationNumber), oldPatientUuid);
            // Add Caste
            String casteValue = getCasteName((Integer) row.get("CasteID"));
            if (!client.isNull(casteValue)) {
                client.addPersonAttribute(patientUuid, "Caste", casteValue, casteUuid);
            }
            // Add Occupation
            if (occupation!=null && !occupation.equals("")) {
                client.addPersonAttribute(patientUuid, "Occupation", occupation, occupationUuid);
            }
            if (education!=null && !education.equals("")) {
                client.addPersonAttribute(patientUuid, "Education", education, educationUuid);
            }

            
            //Is this a TB Patient?
            if (!((String)row.get("P_TB")).equals("0")){
                client.addPersonAttribute(patientUuid, "Registry Type", "TUBERCULOSIS", registryTypeUuid);
            }
            
            // Add weight observation
            Float weight = null;
            if((String) row.get("P_WEIGHT") != null && !((String)row.get("P_WEIGHT")).equals("")) {
                weight = Float.parseFloat((String)row.get("P_WEIGHT"));
            }
            Float height = null;
            if((String)row.get("P_HEIGHT") != null && !((String)row.get("P_HEIGHT")).equals("")) {
                height = Float.parseFloat((String)row.get("P_HEIGHT"));
            }
            Float bmi = null;
            if(weight!=null && height!=null){
                bmi = weight/(height*height);
            }
            client.addRegistrationObservations(patientUuid, (Date)row.get("REG_DATE"), regLocation, height, heightUuid, weight, weightUuid, bmi, bmiUuid, regEncounterTypeUuid);
        }}}catch(Exception e){System.out.println(regEntry);}}
    }

    private void loadCasteIDs() throws FileNotFoundException, IOException {
        casteIDs = new HashMap<Integer, String>();
        CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream("castes.csv")));
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            casteIDs.put(Integer.valueOf(line[0]), line[1]);
        }
    }

    private String getCasteName(Integer casteID) throws FileNotFoundException, IOException {
        if (casteIDs == null) {
            loadCasteIDs();
        }
        return casteIDs.get(casteID);
    }

    private void loadLocationIDs() throws FileNotFoundException, IOException {
        locationIDs = new HashMap<Integer, String[]>();
        CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream("locations.csv")));
        String[] line;
        csvReader.readNext();
        while ((line = csvReader.readNext()) != null) {
            if (line.length == 10) {
                String idString = line[9];
                if (!client.isNull(idString)) {
                    String[] locationArray = new String[4];
                    locationArray[0] = line[7].trim();
                    locationArray[1] = line[5].trim();
                    locationArray[2] = line[3].trim();
                    locationArray[3] = line[0].trim();
                    locationIDs.put(Integer.valueOf(idString), locationArray);
                }
            }
        }
    }

    private String[] getLocationArray(Integer villageID) throws FileNotFoundException, IOException {
        if (locationIDs == null) {
            loadLocationIDs();
        }
        return locationIDs.get(villageID);
    }
 }
