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
package it.polimi.deib.rsp_services_csparql.static_knowledge_base;

import eu.larkc.csparql.common.RDFTable;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_Engine;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class StaticKnowledgeManager extends ServerResource {

	private Logger logger = LoggerFactory.getLogger(StaticKnowledgeManager.class.getName());

	@Post
	public void execUpdateQuery(Representation rep){

		Gson gson = new Gson();
		Form f = new Form(rep);
		
		try {

			Csparql_Engine engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

			String action = f.getFirstValue("action");
			String iri;
			String serialization;
			String queryBody;
			
			switch (action) {
			case "put":
				iri = f.getFirstValue("iri");
				serialization = f.getFirstValue("serialization");
				engine.addStaticModel(iri, serialization);
				break;

			case "delete":
				iri = f.getFirstValue("iri");
				engine.removeStaticModel(iri);
				break;

			case "update":
				queryBody = f.getFirstValue("queryBody");
				engine.execUpdateQueryOverDatasource(queryBody);
				break;

			default:
				throw new Exception();
			}

			this.getResponse().setStatus(Status.SUCCESS_OK,"Update operation succeded.");
			this.getResponse().setEntity(gson.toJson("Update operation succeded."), MediaType.APPLICATION_JSON);

		} catch (Exception e) {
			logger.error("Problem while accessing internal static knowledge base or in the specified action");
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Problem during query Operation");
			this.getResponse().setEntity(gson.toJson("Problem during query Operation"), MediaType.APPLICATION_JSON);
		} finally {
			this.getResponse().commit();
			this.commit();	
			this.release();			
		}

	}

	@Get
	public void evaluateQuery(){
		try {

			Csparql_Engine engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

			String queryBody = getQueryValue("query");

			RDFTable result = engine.evaluateQueryOverDatasource(queryBody);
			String jsonSerialization = result.getJsonSerialization();

			this.getResponse().setStatus(Status.SUCCESS_OK,"Query evaluated.");
			this.getResponse().setEntity((jsonSerialization), MediaType.APPLICATION_JSON);

		} catch (Exception e) {
			logger.error("Problem while accessing internal static knowledge base: {}", e.getMessage());
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Problem during query Operation");
			this.getResponse().setEntity(new Gson().toJson("Problem during query Operation"), MediaType.APPLICATION_JSON);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}

	}



}
