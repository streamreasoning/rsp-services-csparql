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
package it.polimi.deib.rsp_services_csparql.queries;

import it.polimi.deib.rsp_services_csparql.commons.Csparql_Query;
import it.polimi.deib.rsp_services_csparql.observers.utilities.ObserverDescriptorForGet;
import it.polimi.deib.rsp_services_csparql.queries.utilities.CsparqlQueryDescriptionForGet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import it.polimi.deib.rsp_services_csparql.queries.utilities.Csparql_Observer_Descriptor;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class MultipleQueriesDataServer extends ServerResource {

	private static Hashtable<String, Csparql_Query> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(MultipleQueriesDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Options
	public void optionsRequestHandler(){
		String origin = getRequest().getClientInfo().getAddress();
		Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
	}

	@SuppressWarnings({ "unchecked" })
	@Get
	public void getQueriesInformations(){

		try{
			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

			csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
			ArrayList<CsparqlQueryDescriptionForGet> queryDescriptionList = new ArrayList<CsparqlQueryDescriptionForGet>();

			Set<String> keySet = csparqlQueryTable.keySet();
            Csparql_Query csq;
            HashMap<String, Csparql_Observer_Descriptor> om;
            ArrayList<ObserverDescriptorForGet> ol;
			for(String key : keySet){
                csq = csparqlQueryTable.get(key);
                om = csq.getObservers();
				queryDescriptionList.add(
                        new CsparqlQueryDescriptionForGet(csq.getQueryID(), csq.getType(), csq.getStreams(), csq.getQueryBody(), csq.getQueryStatus()));
			}

			this.getResponse().setStatus(Status.SUCCESS_OK,"Information about queries succesfully extracted");
			this.getResponse().setEntity(gson.toJson(queryDescriptionList), MediaType.APPLICATION_JSON);

		} catch(Exception e){
			logger.error("Error while getting multiple queries informations", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while getting multiple queries informations");
			this.getResponse().setEntity(gson.toJson("Error while getting multiple queries informations"), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
}
