/*******************************************************************************
 * Copyright 2013 DEIB - Politecnico di Milano
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
 ******************************************************************************/
package polimi.deib.rsp_service4csparql_server.observer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.rsp_service4csparql_server.query.SingleQueryDataServer;
import polimi.deib.rsp_service4csparql_server.query.utilities.CsparqlObserver;
import polimi.deib.rsp_service4csparql_server.query.utilities.CsparqlQuery;

import com.google.gson.Gson;

public class MultipleObserversDataServer  extends ServerResource {

	private static Hashtable<String, CsparqlQuery> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(SingleQueryDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Get
	public void getObserversInfo(){

		csparqlQueryTable = (Hashtable<String, CsparqlQuery>) getContext().getAttributes().get("csaprqlQueryTable");
		ArrayList<CsparqlObserver> observers = new ArrayList<CsparqlObserver>();

		String queryName = (String) this.getRequest().getAttributes().get("queryname");

		try{
			if(queryName.contains(queryName)){
				CsparqlQuery csparqlQuery = csparqlQueryTable.get(queryName);
				Set<Entry<String, CsparqlObserver>> obsSet = csparqlQuery.getObservers().entrySet();
				for(Entry<String, CsparqlObserver> eObserver : obsSet){
					observers.add(eObserver.getValue());
				}
				this.getResponse().setStatus(Status.SUCCESS_OK,"Observers informations succesfully extracted");
				this.getResponse().setEntity(gson.toJson(observers), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " ID is not associated to any registered query");
				this.getResponse().setEntity(gson.toJson(queryName + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
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
