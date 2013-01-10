/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jss.restClient;

/**
 *
 * @author manorlev-tov
 */
public enum ConceptClass {
    TEST,
    PROCEDURE,
    DRUG,
    DIAGNOSIS,
    FINDING,
    ANATOMY,
    QUESTION,
    LABSET,
    MEDSET,
    CONVSET,
    MISC,
    SYMPTOM,
    SYMPTOMFINDING,
    SPECIMIN,
    MISCORDER;

        
    public String getName() {
        if (this.equals(MISC)) {
            return "8780418d-3ddd-449c-a5dd-9bfa28bb1206";            
        }
        else {
            return this.name();
        }
    }
}
