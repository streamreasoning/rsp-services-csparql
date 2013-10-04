package polimi.deib.rsp_service4csparql_server.stream;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.Hashtable;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;
import polimi.deib.rsp_service4csparql_server.common.Utilities;
import polimi.deib.rsp_service4csparql_server.stream.utilities.CsparqlStream;
import polimi.deib.rsp_service4csparql_server.stream.utilities.CsparqlStreamDescriptionForGet;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlEngine;

public class SingleStreamDataServer extends ServerResource {

	private static Hashtable<String, CsparqlStream> csparqlStreamTable;
	private CsparqlEngine engine;
	private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private Logger logger = LoggerFactory.getLogger(SingleStreamDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Put
	public void registerStream(){

		try{
			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			engine = (CsparqlEngine) getContext().getAttributes().get("csparqlengine");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(!csparqlStreamTable.containsKey(inputStreamName)){
				RdfStream stream = new RdfStream(inputStreamName);
				csparqlStreamTable.put(inputStreamName, new CsparqlStream(stream, CsparqlComponentStatus.RUNNING));
				engine.registerStream(stream);
				getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
				getContext().getAttributes().put("csparqlengine", engine);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully registered");
				this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully registered"), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " already exists");
				this.getResponse().setEntity(gson.toJson(inputStreamName + " already exists"), MediaType.APPLICATION_JSON);
			}
		} catch(Exception e){
			logger.error("Error while registering stream", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}

	@SuppressWarnings("unchecked")
	@Delete
	public void unregisterStream(){
		try{
			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			engine = (CsparqlEngine) getContext().getAttributes().get("csparqlengine");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(csparqlStreamTable.containsKey(inputStreamName)){
				RdfStream stream = csparqlStreamTable.get(inputStreamName).getStream();
				engine.unregisterStream(stream.getIRI());
				csparqlStreamTable.remove(inputStreamName);
				getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
				getContext().getAttributes().put("csparqlengine", engine);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully unregistered");
				this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully unregistered"), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " does not exist");
				this.getResponse().setEntity(gson.toJson(inputStreamName + " does not exist"), MediaType.APPLICATION_JSON);
			}

		} catch(Exception e){
			logger.error("Error while unregistering stream", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

	@SuppressWarnings("unchecked")
	@Post
	public void feedStream(Representation rep){

		try{
			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			engine = (CsparqlEngine) getContext().getAttributes().get("csparqlengine");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(csparqlStreamTable.containsKey(inputStreamName)){
				CsparqlStream streamRepresentation = csparqlStreamTable.get(inputStreamName);

				String jsonSerialization = rep.getText();

				Model model = ModelFactory.createDefaultModel();

				try{
					model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"RDF/JSON");
					long ts = System.currentTimeMillis();

					StmtIterator it = model.listStatements();
					while(it.hasNext()){
						Statement st = it.next();
						streamRepresentation.feedStream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
					}

					this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
					this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
				} catch(Exception e){
					try{
						model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"RDF/XML");
						long ts = System.currentTimeMillis();

						StmtIterator it = model.listStatements();
						while(it.hasNext()){
							Statement st = it.next();
							streamRepresentation.getStream().put(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
						}

						this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
						this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
					} catch(Exception e1){
						try{
							model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"N-TRIPLE");
							long ts = System.currentTimeMillis();

							StmtIterator it = model.listStatements();
							while(it.hasNext()){
								Statement st = it.next();
								streamRepresentation.getStream().put(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
							}

							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
						} catch(Exception e2){
							model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"TURTLE");
							long ts = System.currentTimeMillis();

							StmtIterator it = model.listStatements();
							while(it.hasNext()){
								Statement st = it.next();
								streamRepresentation.getStream().put(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
							}

							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
						}
					}
				}

			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Specified stream does not exists");
				this.getResponse().setEntity(gson.toJson("Specified stream does not exists"), MediaType.APPLICATION_JSON);
			}

		} catch(Exception e){
			logger.error("Error while changing status of a stream", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

		//		try{
		//			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");
		//			engine = (CsparqlEngine) getContext().getAttributes().get("csparqlengine");
		//
		//			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");
		//			Form f = new Form(rep);
		//			String action = f.getFirstValue("action");
		//
		//			if(csparqlStreamTable.containsKey(inputStreamName)){
		//				CsparqlStream streamRepresentation = csparqlStreamTable.get(inputStreamName);
		//				if(action.equals("feed")){
		//					
		//					String jsonSerialization = f.getFirstValue("jsonSerialization");
		//					Model model = ModelFactory.createDefaultModel();
		//					model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"RDF/JSON");
		//					long ts = System.currentTimeMillis();
		//					
		//					
		//					StmtIterator it = model.listStatements();
		//					while(it.hasNext()){
		//						Statement st = it.next();
		//						streamRepresentation.getStream().put(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
		//					}
		//					
		//					this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
		//					this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
		//				}
		//				else{
		//					if(action.equals("pause") && streamRepresentation.getStatus().equals(CsparqlComponentStatus.PAUSED)){
		//						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Stream " + inputStreamName + " already paused");
		//						this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " already paused"), MediaType.APPLICATION_JSON);
		//					} else if(action.equals("restart") && streamRepresentation.getStatus().equals(CsparqlComponentStatus.RUNNING)){
		//						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Stream " + inputStreamName + " already running");
		//						this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " already running"), MediaType.APPLICATION_JSON);
		//					} else {
		//						if(action.equals("pause")){
		//							streamRepresentation.setStatus(CsparqlComponentStatus.PAUSED);
		//							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " paused");
		//							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully paused"), MediaType.APPLICATION_JSON);
		//						} else if (action.equals("restart")){
		//							streamRepresentation.setStatus(CsparqlComponentStatus.RUNNING);
		//							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " restarted");
		//							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully restarted"), MediaType.APPLICATION_JSON);
		//						} else {
		//							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Input parameters error");
		//							this.getResponse().setEntity(gson.toJson("Input parameters error"), MediaType.APPLICATION_JSON);
		//						}
		//					}
		//				}
		//			} else {
		//				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Specified stream does not exists");
		//				this.getResponse().setEntity(gson.toJson("Specified stream does not exists"), MediaType.APPLICATION_JSON);
		//			}
		//
		//		} catch(Exception e){
		//			logger.error("Error while changing status of a stream", e);
		//			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Generic Error");
		//			this.getResponse().setEntity(gson.toJson("Generic Error"), MediaType.APPLICATION_JSON);
		//		} finally{
		//			this.getResponse().commit();
		//			this.commit();	
		//			this.release();
		//		}

	}

	@SuppressWarnings("unchecked")
	@Get
	public void getStreamInformations(){

		try{

			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(csparqlStreamTable.containsKey(inputStreamName)){
				CsparqlStream streamRepresentation = csparqlStreamTable.get(inputStreamName);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Information about " + inputStreamName + " succesfully extracted");
				this.getResponse().setEntity(gson.toJson(new CsparqlStreamDescriptionForGet(streamRepresentation.getStream().getIRI(), streamRepresentation.getStatus())), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " does not exists");
				this.getResponse().setEntity(gson.toJson(inputStreamName + " does not exists"), MediaType.APPLICATION_JSON);
			}

		} catch(Exception e){
			logger.error("Error while getting stream informations", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
}
