/*******************************************************************************
 * Copyright 2014 DEIB - Politecnico di Milano
 *  
 * Marco Balduini (marco.balduini@polimi.it)
 * Emanuele Della Valle (emanuele.dellavalle@polimi.it)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work was partially supported by the European project LarKC (FP7-215535) and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package it.polimi.deib.rsp_services_csparql.streams.utilities;

import org.streamreasoning.rsp_services.commons.Rsp_services_Component_Status;

public class CsparqlStreamDescriptionForGet {

    private String name;
	private String streamIRI;
	private Rsp_services_Component_Status status;

    public CsparqlStreamDescriptionForGet(String name, String streamIRI, Rsp_services_Component_Status status) {
        this.name = name;
        this.streamIRI = streamIRI;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreamIRI() {
        return streamIRI;
    }

    public void setStreamIRI(String streamIRI) {
        this.streamIRI = streamIRI;
    }

    public Rsp_services_Component_Status getStatus() {
        return status;
    }

    public void setStatus(Rsp_services_Component_Status status) {
        this.status = status;
    }
}
