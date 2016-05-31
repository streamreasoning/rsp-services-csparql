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
package it.polimi.deib.rsp_services_csparql.observers;

import it.polimi.deib.rsp_services_csparql.commons.Csparql_Query;
import it.polimi.deib.rsp_services_csparql.observers.utilities.ObserverDescriptorForGet;
import it.polimi.deib.rsp_services_csparql.queries.SingleQueryDataServer;
import it.polimi.deib.rsp_services_csparql.queries.utilities.Csparql_Observer_Descriptor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

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

public class MultipleObserversDataServer  extends ServerResource {

	private static Hashtable<String, Csparql_Query> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(SingleQueryDataServer.class.getName());


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

	@Get
	public void getObserversInfo(){

		csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
		ArrayList<ObserverDescriptorForGet> observers = new ArrayList<ObserverDescriptorForGet>();

		String queryName = (String) this.getRequest().getAttributes().get("queryname");

		String origin = getRequest().getClientInfo().getAddress();
		Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
		
		try{
			if(csparqlQueryTable.containsKey(queryName)){
				Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
				Set<Entry<String, Csparql_Observer_Descriptor>> obsSet = csparqlQuery.getObservers().entrySet();
				for(Entry<String, Csparql_Observer_Descriptor> eObserver : obsSet){
					observers.add(new ObserverDescriptorForGet(eObserver.getValue().getId(),
                            eObserver.getValue().getType(),
                            "/queries/" + csparqlQuery.getName() + "/observers/" + eObserver.getValue().getId()));
				}
				this.getResponse().setStatus(Status.SUCCESS_OK,"Observers informations succesfully extracted");
				this.getResponse().setEntity(gson.toJson(observers), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " is not associated to any registered query");
				this.getResponse().setEntity(gson.toJson(queryName + " is not associated to any registered query"), MediaType.APPLICATION_JSON);
			}
		} catch (Exception e) {
			logger.error("Error while getting observers information");
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while getting observers information");
			this.getResponse().setEntity(gson.toJson("Error while getting observers information"), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}
}
