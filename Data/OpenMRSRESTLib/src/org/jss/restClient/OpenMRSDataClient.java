/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jss.restClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*; 
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 *
 * @author manorlev-tov
 */
public class OpenMRSDataClient {

    private final OpenMRSRestClient client;
    private final BaseData baseData;
    private final boolean verbose;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public OpenMRSDataClient(OpenMRSRestClient client)
            throws IOException, JSONException {
        this(client, false);
    }

    public OpenMRSDataClient(OpenMRSRestClient client, boolean verbose)
            throws IOException, JSONException {
        this.client = client;
        this.verbose = verbose;
        this.baseData = new BaseData(client);
    }

//    public void addPersonAttributeType(String name, String description, String format, boolean searchable)
//            throws JSONException, IOException {
//        // only add if it does not already exist
//        if (client.getObjectBySearch("personattributetype", StringEscapeUtils.escapeHtml3(name)).isEmpty()) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            jsRequest.key("name").value(name);
//            jsRequest.key("description").value(description);
//            jsRequest.key("format").value(format);
//            jsRequest.key("searchable").value(searchable);
//            jsRequest.endObject();
//            createAndPrintResult("personattributetype", jsRequest);
//        }
//    }

//    public void addPersonAddress(String personUuid, String stateName, String districtName, String tahsilName, String villageName)
//            throws IOException, JSONException {
//        JSONObject location = baseData.getLocationJson(stateName, districtName, tahsilName, villageName);
//        if (location != null) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            jsRequest.key("cityVillage").value(location.getString("cityVillage"));
//            jsRequest.key("address1").value(location.getString("address1"));
//            jsRequest.key("countyDistrict").value(location.getString("countyDistrict"));
//            jsRequest.key("stateProvince").value(location.getString("stateProvince"));
//            jsRequest.key("address2").value(location.getString("address1"));
//            jsRequest.key("address3").value(location.getString("address1"));
//            jsRequest.key("address5").value(location.getString("address1"));
//            jsRequest.key("address6").value(location.getString("address1"));
//            jsRequest.key("country").value("India");
//            jsRequest.key("preferred").value(true);
//        }
//    }

//    public void addPersonAttributeConcept(String personUuid, String name, String value)
//            throws IOException, JSONException {
//        addPersonAttribute(personUuid, name, baseData.getConceptUuid(value));
//    }

    public void addPersonAttribute(String personUuid, String name, String value, String uuid)
            throws IOException, JSONException {
        String attributeTypeUuid = uuid;
        JSONObject personJson = client.getObjectByUUID("person", personUuid);

        // Does attributeType already exist for this person
        boolean exists = false;
        JSONArray attributes = personJson.getJSONArray("attributes");
        for (int x = 0; x < attributes.length(); x++) {
            JSONObject attribute = attributes.getJSONObject(x);
            String display = attribute.getString("display");
            if (display.equals(name + " = " + value)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            JSONStringer jsRequest = new JSONStringer();
            jsRequest.object();
            jsRequest.key("attributeType").value(attributeTypeUuid);
            jsRequest.key("value").value(value);
            jsRequest.endObject();
            createAndPrintResult("person", personUuid, "attribute", jsRequest);
        }
    }

//    public void addConcept(String name, ConceptDatatype type, ConceptClass conceptClass, boolean set)
//            throws JSONException, IOException {
//
//        if (client.getObjectBySearch("concept", StringEscapeUtils.escapeHtml3(name)).isEmpty()) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            
//            HashMap[] hm = {new HashMap()}; 
//            hm[0].put("name", name);
//            hm[0].put("locale", "en");
//            hm[0].put("conceptNameType", "FULLY_SPECIFIED");
//            
//            
//            jsRequest.key("names").value(hm);
//            //jsRequest.key("datatype").value(type);
//            //jsRequest.key("locale").value("en");
//            //jsRequest.key("conceptNameType").value("FULLY_SPECIFIED");
//            jsRequest.key("datatype").value(type.getName());
//            jsRequest.key("conceptClass").value(conceptClass.getName());
//            jsRequest.key("set").value(set);
//            jsRequest.endObject();
//            createAndPrintResult("concept", jsRequest);
//        }
//    }

    private String createAndPrintResult(String type, String parentUuid, String subType, JSONStringer js) throws IOException, JSONException {
        return printResult(client.createNewSubObject(type, parentUuid, subType, js));
    }

    private String createAndPrintResult(String type, JSONStringer js) throws IOException, JSONException {
        return printResult(client.createNewObject(type, js));
    }

    private String printResult(Representation r) throws IOException, JSONException {
        JSONObject jo = (new JsonRepresentation(r).getJsonObject());
        if (verbose) {
            String output = "Added " + jo.get("uuid");
            if (!jo.isNull("name")) {
                output = output + ":" + jo.getString("name");
            }
            //System.out.println(output);
        }
        return jo.getString("uuid");
    }

    public boolean isNull(String value) {
        if (value == null) {
            return true;
        } else {
            return value.isEmpty() || value.equalsIgnoreCase("null") || value.equals("None");
        }
    }

//    public String addLocationState(String stateName)
//            throws JSONException, IOException {
//        String result = "";
//        result = baseData.getLocation(stateName, null, null, null);
//        if (result == null) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            jsRequest.key("name").value(stateName);
//            jsRequest.key("stateProvince").value(stateName);
//            jsRequest.key("country").value("India");
//            jsRequest.key("description").value("State");
//            jsRequest.endObject();
//            return createAndPrintResult("location", jsRequest);
//        }
//        return result;
//    }

//    public String addLocationDistrict(String parent, String districtName, String districtID, String stateName)
//            throws JSONException, IOException {
//
//        String result = "";
//        result = baseData.getLocation(stateName, districtName, null, null);
//
//        if (result == null) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            jsRequest.key("parentLocation").value(parent);
//            jsRequest.key("name").value(districtName);
//            jsRequest.key("countyDistrict").value(districtName);
//            jsRequest.key("stateProvince").value(stateName);
//            jsRequest.key("address6").value(districtID);
//            jsRequest.key("country").value("India");
//            jsRequest.key("description").value("District");
//            jsRequest.endObject();
//            return createAndPrintResult("location", jsRequest);
//        }
//        return result;
//    }

//    public String addLocationTahsil(String parent, String tahsilName, String tahsilID, String districtName, String districtID, String stateName)
//            throws JSONException, IOException {
//
//        String result = "";
//        result = baseData.getLocation(stateName, districtName, tahsilName, null);
//
//        if (result == null) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            jsRequest.key("parentLocation").value(parent);
//            jsRequest.key("name").value(tahsilName);
//            jsRequest.key("address2").value(tahsilName);
//            jsRequest.key("address5").value(tahsilID);
//            jsRequest.key("countyDistrict").value(districtName);
//            jsRequest.key("stateProvince").value(stateName);
//            jsRequest.key("address6").value(districtID);
//            jsRequest.key("country").value("India");
//            jsRequest.key("description").value("Tahsil");
//            jsRequest.endObject();
//            return createAndPrintResult("location", jsRequest);
//        }
//        return result;
//    }

//    public String addLocationVillage(String parent, String villageName, String villageID, String gramPanchayat, String tahsilName, String tahsilID, String districtName, String districtID, String stateName)
//            throws JSONException, IOException {
//        String result = baseData.getLocation(stateName, districtName, tahsilName, villageName);
//
//        if (result == null) {
//            JSONStringer jsRequest = new JSONStringer();
//            jsRequest.object();
//            jsRequest.key("parentLocation").value(parent);
//            jsRequest.key("name").value(villageName);
//            jsRequest.key("cityVillage").value(villageName);
//            if (!gramPanchayat.isEmpty()) {
//                jsRequest.key("address1").value(gramPanchayat);
//            }
//            jsRequest.key("address3").value(villageID);
//            jsRequest.key("address2").value(tahsilName);
//            jsRequest.key("address5").value(tahsilID);
//            jsRequest.key("countyDistrict").value(districtName);
//            jsRequest.key("stateProvince").value(stateName);
//            jsRequest.key("address6").value(districtID);
//            jsRequest.key("country").value("India");
//            jsRequest.key("description").value("Village");
//            jsRequest.endObject();
//            return createAndPrintResult("location", jsRequest);
//        }
//        return result;
//    }

//    public String JssRegistrationIdentifierType() throws IOException, JSONException {
//        JSONStringer jsRequest = new JSONStringer();
//        jsRequest.object();
//        jsRequest.key("name").value("JSS Registration Number");
//        jsRequest.key("display").value("JSS Registration Number");
//        jsRequest.key("required").value(true);
//        jsRequest.key("checkDigit").value(true);
//        jsRequest.endObject();
//        return createAndPrintResult("patientidentifierattributetype", jsRequest);
//    }

    public String addPatient(String personUuid, String idNumber, String loc)
            throws IOException, JSONException {
        JSONStringer jsRequest = new JSONStringer();
        jsRequest.object();
        jsRequest.key("person").value(personUuid);
        jsRequest.key("identifiers").array();
        jsRequest.object();
        jsRequest.key("identifier").value(idNumber);
        jsRequest.key("identifierType").value("2debe053-92e5-4f1e-af8b-0f94e8df664f");
        jsRequest.key("location").value(loc);
        jsRequest.endObject();
        jsRequest.endArray();
        jsRequest.endObject();
        return createAndPrintResult("patient", jsRequest);
        
    }
    
    public String newPatientIdentifier(String regLocString) throws IOException, JSONException{
        while(true){
            Random randomGenerator = new Random();
            Integer randomInt = randomGenerator.nextInt(1000000);
            ArrayList<JSONObject> arr = client.getObjectBySearch("patient", regLocString+randomInt.toString());
            if(!arr.isEmpty()) {
            }
            else {
                return regLocString+randomInt.toString();
            }
        }
    }
    
//    public String addNonPatient(String firstName, String lastName, String gender, Date birthdate)
//            throws IOException, JSONException {
//        return addPerson(firstName, lastName, "", SubCenterLocation.GANIYARI, gender, birthdate, false);
//    }
    
    public String addPerson(String firstName, String lastName, String jssRegistrationNumber, String registrationLocation, String gender, Date birthdate, String stateName, String districtName, String tahsilName, String villageName)
            throws IOException, JSONException {
        JSONStringer jsRequest = new JSONStringer();
        jsRequest.object();
        jsRequest.key("names").array();
        jsRequest.object();
        jsRequest.key("givenName").value(firstName);
        jsRequest.key("familyName").value(lastName);
        jsRequest.key("preferred").value("true");
        jsRequest.endObject();
        jsRequest.endArray();
        if (birthdate != null){
          jsRequest.key("birthdate").value(dateFormatter.format(birthdate));
        }
        jsRequest.key("addresses").array();
        jsRequest.object();
        jsRequest.key("preferred").value("true");
        jsRequest.key("address1").value(villageName);
        jsRequest.key("address2").value(tahsilName);
        jsRequest.key("cityVillage").value(villageName);
        jsRequest.key("stateProvince").value(stateName);
        jsRequest.key("country").value("India");
        jsRequest.endObject();
        jsRequest.endArray();
        jsRequest.key("gender").value(gender);
        jsRequest.endObject();
        return createAndPrintResult("person", jsRequest);
    }
    
    public String addEncounterType(String name, String description)
            throws IOException, JSONException {
//        ArrayList<JSONObject> encounterTypes = client.getObjectBySearch("encountertype", name.replace(" ", "%20"));
        ArrayList<JSONObject> encounterTypes = client.getObjectBySearch("encountertype", name);
        if (encounterTypes.isEmpty()){
            JSONStringer jsRequest = new JSONStringer();
            jsRequest.object();
            jsRequest.key("name").value(name);
            jsRequest.key("description").value(description);
            jsRequest.endObject();
            return createAndPrintResult("encountertype", jsRequest);
        } else {
            return encounterTypes.get(0).getString("uuid");
        }        
    }

//    public void addCodedRegistrationObservation(String patientUuid, Date observationDate, String subCenterLocation, String concept, String value) throws IOException, JSONException{
//       ArrayList<JSONObject> values = client.getObjectBySearch("concept", value);
//       for (JSONObject valueJson : values) {
//           if (valueJson.getString("display").equalsIgnoreCase(value)) {
//               value = valueJson.getString("uuid");
//               break;
//           }
//       }
//       addRegistrationObservation(patientUuid, observationDate, subCenterLocation, concept, value);
//    }
            
    public void addRegistrationObservations(String patientUuid, Date observationDate, String subCenterLocation, Float height, String heightUuid, Float weight, String weightUuid, Float bmi, String bmiUuid, String regEncounterTypeUuid)
            throws IOException, JSONException {
        // Add encounter with registration encounter type
        JSONStringer jsRequest = new JSONStringer();
        jsRequest.object();
        jsRequest.key("location").value(subCenterLocation);
        jsRequest.key("encounterType").value(regEncounterTypeUuid);
        jsRequest.key("patient").value(patientUuid);
        jsRequest.key("encounterDatetime").value(dateFormatter.format(observationDate));
        jsRequest.key("provider").value("d3e9eaac-e46a-11e1-87dc-70f39542ef8f");
        jsRequest.key("obs").array();
        if(height!=null){
            jsRequest.object();
            jsRequest.key("person").value(patientUuid);
            jsRequest.key("obsDatetime").value(dateFormatter.format(observationDate));
            jsRequest.key("concept").value(heightUuid);
            jsRequest.key("value").value(height);
            jsRequest.endObject();
        }
        if(weight!=null){
            jsRequest.object();
            jsRequest.key("person").value(patientUuid);
            jsRequest.key("obsDatetime").value(dateFormatter.format(observationDate));
            jsRequest.key("concept").value(weightUuid);
            jsRequest.key("value").value(weight);
            jsRequest.endObject();
        }
        if(bmi!=null){
            jsRequest.object();
            jsRequest.key("person").value(patientUuid);
            jsRequest.key("obsDatetime").value(dateFormatter.format(observationDate));
            jsRequest.key("concept").value(bmiUuid);
            jsRequest.key("value").value(bmi);
            jsRequest.endObject();
        }
        jsRequest.endArray();
        jsRequest.endObject();
        createAndPrintResult("encounter", jsRequest);
    }

    public String getPatientUuidByRegistrationNumber(String searchString) throws IOException, JSONException {
        ArrayList<JSONObject> results = client.getObjectBySearch("patient", searchString);
        return results.get(0).getString("uuid");
    }
    
    public String getUuidByDisplay(String searchObj, String searchString, String display) throws IOException, JSONException {
        ArrayList<JSONObject> results = client.getObjectBySearch(searchObj, searchString);
        for(int i=0; i<results.size(); i++){
            if(results.get(i).get("display").toString().contains(display)){
                return results.get(i).get("uuid").toString();
            }
        }
        return null;
    }
    
    public String getUuidByDisplayNoSearch(String searchObj, String display) throws IOException, JSONException {
        ArrayList<JSONObject> results = client.getAllObjects(searchObj);
        for(int i=0; i<results.size(); i++){
            if(results.get(i).get("display").toString().contains(display)){
                return results.get(i).get("uuid").toString();
            }
        }
        return null;
    }    
}
