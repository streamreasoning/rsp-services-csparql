package polimi.deib.rsp_service4csparql_server.knowledge_base;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.larkc.csparql.core.engine.CsparqlEngine;

public class Update extends ServerResource {

	private Logger logger = LoggerFactory.getLogger(Update.class.getName());

	@Post
	public void execUpdateQuery(Representation rep){
		
		Gson gson = new Gson();
		
		try {

			CsparqlEngine engine = (CsparqlEngine) getContext().getAttributes().get("csparqlengine");

			String queryBody;
			queryBody = rep.getText();
			engine.execUpdateQueryOverDatasource(queryBody);
			this.getResponse().setStatus(Status.SUCCESS_OK,"Update operation succeded.");
			this.getResponse().setEntity(gson.toJson("Update operation succeded."), MediaType.APPLICATION_JSON);
			

		} catch (IOException e) {
			logger.error("Problem during query Operation");
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Problem during query Operation");
			this.getResponse().setEntity(gson.toJson("Problem during query Operation"), MediaType.APPLICATION_JSON);
		} finally {

			this.getResponse().commit();
			this.commit();	
			this.release();			
		}

	}


}
