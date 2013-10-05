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
package polimi.deib.rsp_service4csparql_server;

import java.util.Hashtable;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.rsp_service4csparql_server.configuration.Config;
import polimi.deib.rsp_service4csparql_server.knowledge_base.Update;
import polimi.deib.rsp_service4csparql_server.observer.MultipleObserversDataServer;
import polimi.deib.rsp_service4csparql_server.observer.SingleObserverDataServer;
import polimi.deib.rsp_service4csparql_server.query.MultipleQueriesDataServer;
import polimi.deib.rsp_service4csparql_server.query.SingleQueryDataServer;
import polimi.deib.rsp_service4csparql_server.query.utilities.CsparqlQuery;
import polimi.deib.rsp_service4csparql_server.stream.MultipleStreamsDataServer;
import polimi.deib.rsp_service4csparql_server.stream.SingleStreamDataServer;
import polimi.deib.rsp_service4csparql_server.stream.utilities.CsparqlStream;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

public class rsp_service4csparql_server extends Application{

	private static Component component;
	private static CsparqlEngine engine = null;
	private static Hashtable<String, CsparqlStream> csparqlStreamTable = new Hashtable<String, CsparqlStream>();
	private static Hashtable<String, CsparqlQuery> csparqlQueryTable = new Hashtable<String, CsparqlQuery>();
	
	private static Logger logger = LoggerFactory.getLogger(rsp_service4csparql_server.class.getName());

	public static void main(String[] args) throws Exception{

		engine = new CsparqlEngineImpl();

		if(Config.getInstance().getEnableTSFunction()){
			logger.debug("Timestamp function enabled");
			engine.initialize(true);
		} else {
			logger.debug("Timestamp function disbaled");
			engine.initialize(false);
		}

		if(Config.getInstance().getActivateInference()){
			logger.debug("Inference enabled");
			engine.activateInference();
			engine.setInferenceRulesFilePath(Config.getInstance().getInferenceRulesFile());
		}

		component = new Component();
		component.getServers().add(Protocol.HTTP, Config.getInstance().getServerPort());
		component.getClients().add(Protocol.FILE);  

		rsp_service4csparql_server csparqlServer = new rsp_service4csparql_server();
		component.getDefaultHost().attach("", csparqlServer);
		
		component.start();

	}

	public Restlet createInboundRoot(){

		String server_address = component.getServers().get(0).getAddress();
		if(server_address == null){
			server_address = "http://localhost";
			server_address = server_address + ":" + String.valueOf(component.getServers().get(0).getActualPort());
		}

		getContext().getAttributes().put("complete_server_address", server_address);
		getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
		getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
		getContext().getAttributes().put("csparqlengine", engine);

		Router router = new Router(getContext());
		router.setDefaultMatchingMode(Template.MODE_EQUALS);

		router.attach("/streams", MultipleStreamsDataServer.class);
		router.attach("/streams/{streamname}", SingleStreamDataServer.class);
		router.attach("/queries", MultipleQueriesDataServer.class);
		router.attach("/queries/{queryname}", SingleQueryDataServer.class);
		router.attach("/queries/{queryname}/observers", MultipleObserversDataServer.class);
		router.attach("/queries/{queryname}/observers/{id}", SingleObserverDataServer.class);

		router.attach("/updatekb", Update.class);

		return router;
	}

}
