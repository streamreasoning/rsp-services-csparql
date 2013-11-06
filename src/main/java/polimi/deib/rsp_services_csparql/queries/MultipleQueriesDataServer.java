/*******************************************************************************
 * Copyright 2013 Marco Balduini, Emanuele Della Valle
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
 * Acknowledgements:
 * 
 * This work was partially supported by the European project LarKC (FP7-215535) 
 * and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package polimi.deib.rsp_services_csparql.queries;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.rsp_services_csparql.queries.utilities.CsparqlQuery;
import polimi.deib.rsp_services_csparql.queries.utilities.CsparqlQueryDescriptionForGet;

import com.google.gson.Gson;

public class MultipleQueriesDataServer extends ServerResource {

	private static Hashtable<String, CsparqlQuery> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(MultipleQueriesDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Get
	public void getQueriesInformations(){
		
		try{
			csparqlQueryTable = (Hashtable<String, CsparqlQuery>) getContext().getAttributes().get("csaprqlQueryTable");
			ArrayList<CsparqlQueryDescriptionForGet> queryDescriptionList = new ArrayList<CsparqlQueryDescriptionForGet>();

			Set<String> keySet = csparqlQueryTable.keySet();
			CsparqlQuery registeredCsparqlQuery;
			for(String key : keySet){
				registeredCsparqlQuery = csparqlQueryTable.get(key); 
				queryDescriptionList.add(new CsparqlQueryDescriptionForGet(registeredCsparqlQuery.getId(), registeredCsparqlQuery.getName(), registeredCsparqlQuery.getType(), registeredCsparqlQuery.getStreams(), registeredCsparqlQuery.getBody(), registeredCsparqlQuery.getStatus()));
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
