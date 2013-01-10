package org.jss.restClient;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author manorlev-tov
 */
public enum ConceptDatatype {
    NUMERIC,
    CODED,
    TEXT,
    NA,
    DOCUMENT,
    DATE,
    TIME,
    DATETIME,
    BOOLEAN,
    RULE,
    COMPLEX;
    
    public String getName() {
        if (this.equals(NA)) {
            return "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f";            
        }
        else {
            return this.name();
        }
    }
}
