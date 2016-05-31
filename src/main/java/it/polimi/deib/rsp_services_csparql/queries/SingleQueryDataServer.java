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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_Engine;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_Query;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_RDF_Stream;
import it.polimi.deib.rsp_services_csparql.configuration.Config;
import it.polimi.deib.rsp_services_csparql.observers.utilities.HTTP.Observer4HTTP;
import it.polimi.deib.rsp_services_csparql.observers.utilities.websocket.Observer4WS;
import it.polimi.deib.rsp_services_csparql.queries.utilities.CsparqlQueryDescriptionForGet;
import it.polimi.deib.rsp_services_csparql.queries.utilities.Csparql_Observer_Descriptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.commons.Rsp_services_Component_Status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import eu.larkc.csparql.core.engine.RDFStreamFormatter;

import javax.xml.datatype.DatatypeFactory;

public class SingleQueryDataServer extends ServerResource {

    private static Hashtable<String, Csparql_Query> csparqlQueryTable;
    private static Hashtable<String, Csparql_RDF_Stream> csparqlStreamTable;
    private Csparql_Engine engine;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

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

    @SuppressWarnings({ "unchecked" })
    @Get
    public void getQueryInformations(){

        try{

            String origin = getRequest().getClientInfo().getAddress();
            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

            csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
            String queryName = (String) this.getRequest().getAttributes().get("queryname");
            if(csparqlQueryTable.containsKey(queryName)){
                Csparql_Query csq = csparqlQueryTable.get(queryName);
                this.getResponse().setStatus(Status.SUCCESS_OK,"Information about streams succesfully extracted");
                this.getResponse().setEntity(gson.toJson(new CsparqlQueryDescriptionForGet(csq.getQueryID(), csq.getType(), csq.getStreams(), csq.getQueryBody(), csq.getQueryStatus())), MediaType.APPLICATION_JSON);
            } else {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"A query with specified name does not exist");
                this.getResponse().setEntity(gson.toJson("A query with specified name does not exist"), MediaType.APPLICATION_JSON);
            }
        } catch(Exception e){
            logger.error("Error while getting query information", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Generic Error");
            this.getResponse().setEntity(gson.toJson("Generic Error"), MediaType.APPLICATION_JSON);
        } finally{
            this.getResponse().commit();
            this.commit();
            this.release();
        }

    }

    @SuppressWarnings({ "unchecked" })
    @Put
    public void registerQuery(Representation rep){
        try{

            ArrayList<String> inputStreamNameList;
            String extractedQueryName;
            String parameterQueryName;
            boolean queryStreamWellRegistered = true;

            Form f = new Form(rep);

            csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
            csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
            engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");
            String hostName = Config.getInstance().getHostName();

            String origin = getRequest().getClientInfo().getAddress();
            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

            parameterQueryName = (String) this.getRequest().getAttributes().get("queryname");
            String queryBody = f.getFirstValue("queryBody");

            extractedQueryName = extractNameFromQuery(queryBody);
            inputStreamNameList = extractStreamNamesFromQuery(queryBody);

            if(parameterQueryName.equals(extractedQueryName)){
                if(!csparqlQueryTable.containsKey(parameterQueryName)){
                    if(checkInputStream(inputStreamNameList)){
                        Model completeStaticInfo = ModelFactory.createDefaultModel();
                        for(String s : inputStreamNameList){
                            Csparql_RDF_Stream stream = csparqlStreamTable.get(s);
                            if(stream.gettBox() != null)
                                completeStaticInfo.add(stream.gettBox());
                            if(stream.getStaticAbox() != null)
                                completeStaticInfo.add(stream.getStaticAbox());
                        }

                        String queryType = extractQueryType(queryBody);
                        queryBody = rewriteQuery(queryBody, inputStreamNameList);

                        if(queryType.equals("stream")){
                            String newStreamName = Config.getInstance().getHostName() + "streams/" + parameterQueryName;
                            if(!csparqlStreamTable.contains(parameterQueryName)){
                                CsparqlQueryResultProxy rp;
                                if(completeStaticInfo != null && !completeStaticInfo.isEmpty())
                                    rp = (CsparqlQueryResultProxy) engine.registerQuery(queryBody, completeStaticInfo);
                                else
                                    rp = (CsparqlQueryResultProxy) engine.registerQuery(queryBody);
                                csparqlQueryTable.put(parameterQueryName, new Csparql_Query(rp.getId(), parameterQueryName, queryType, inputStreamNameList, queryBody, rp, new HashMap<String, Csparql_Observer_Descriptor>(), Rsp_services_Component_Status.RUNNING));
                                RDFStreamFormatter stream = new RDFStreamFormatter(newStreamName);
                                csparqlStreamTable.put(parameterQueryName, new Csparql_RDF_Stream(parameterQueryName, newStreamName, stream, Rsp_services_Component_Status.RUNNING));
                                RdfStream rdfStream = null;
                                try{
                                    rdfStream = (RdfStream) engine.registerStream(stream);
                                } catch (Exception e){
                                    queryStreamWellRegistered = false;
                                }
                                if(rdfStream == null){
                                    queryStreamWellRegistered = false;
                                }
                                if(!queryStreamWellRegistered){
                                    engine.unregisterQuery(rp.getId());
                                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while registering stream created by stream query");
                                    this.getResponse().setEntity(gson.toJson("Error while registering stream created by stream query"), MediaType.APPLICATION_JSON);
                                } else {
                                    rp.addObserver(stream);
                                    getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
                                    getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
                                    getContext().getAttributes().put("csparqlengine", engine);
                                    this.getResponse().setStatus(Status.SUCCESS_OK,"Query and stream " + newStreamName + " succesfully registered");
                                    this.getResponse().setEntity(gson.toJson(parameterQueryName), MediaType.APPLICATION_JSON);
                                }
                            } else {
                                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Stream query name corresponds to a stream already registered. Change query name.");
                                this.getResponse().setEntity(gson.toJson("Stream query name corresponds to a stream already registered. Change query name."), MediaType.APPLICATION_JSON);
                            }
                        } else {
                            CsparqlQueryResultProxy rp;
                            if(completeStaticInfo != null && !completeStaticInfo.isEmpty())
                                rp = (CsparqlQueryResultProxy) engine.registerQuery(queryBody, completeStaticInfo);
                            else
                                rp = (CsparqlQueryResultProxy) engine.registerQuery(queryBody);
                            csparqlQueryTable.put(parameterQueryName, new Csparql_Query(rp.getId(), parameterQueryName, queryType, inputStreamNameList, queryBody, rp, new HashMap<String, Csparql_Observer_Descriptor>(), Rsp_services_Component_Status.RUNNING));

                            //test
                            //							rp.addObserver(new ConsoleFormatter());

                            getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
                            getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
                            getContext().getAttributes().put("csparqlengine", engine);
                            this.getResponse().setStatus(Status.SUCCESS_OK,"Query " + parameterQueryName + " succesfully registered");
                            this.getResponse().setEntity(gson.toJson("Query " + parameterQueryName + " succesfully registered"), MediaType.APPLICATION_JSON);
                        }

                        //						if(parameterQueryName.contains("isInWith")){
                        //							Csparql_Observer_Descriptor csObs = new Csparql_Observer_Descriptor("abc", new LocalResultObserver());
                        //							csparqlQueryTable.get(parameterQueryName).addObserver(csObs);
                        //						}

                    } else {
                        this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"One or more of the specified input stream not exist");
                        this.getResponse().setEntity(gson.toJson("One or more of the specified input stream not exist"), MediaType.APPLICATION_JSON);
                    }
                }else {
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Query with same name already exists");
                    this.getResponse().setEntity(gson.toJson("Query with same name already exists"), MediaType.APPLICATION_JSON);
                }
            } else {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Query name specified in the uri must be equals to the name contained in the query body");
                this.getResponse().setEntity(gson.toJson("Query name specified in the uri must be equals to the name contained in the query body"), MediaType.APPLICATION_JSON);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing query",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while parsing query");
            this.getResponse().setEntity(gson.toJson("Error while parsing query"), MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            logger.error("Error while reading query body",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while reading query body");
            this.getResponse().setEntity(gson.toJson("Error while reading query body"), MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            logger.error("Error while reading query body",e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while registering query body");
            this.getResponse().setEntity(gson.toJson("Error while registering query body"), MediaType.APPLICATION_JSON);
        } finally{
            this.getResponse().commit();
            this.commit();
            this.release();
        }

    }

    @SuppressWarnings({ "unchecked" })
    @Delete
    public void unregisterQuery(){

        String queryName = new String();

        try{
            csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
            csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
            engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");
            String hostName = Config.getInstance().getHostName();

            queryName = (String) this.getRequest().getAttributes().get("queryname");

            String origin = getRequest().getClientInfo().getAddress();
            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

            if(csparqlQueryTable.containsKey(queryName)){
                Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
                if(csparqlQuery.getType().equals("stream")){
                    String newStreamName = Config.getInstance().getHostName()  + "streams/" + csparqlQuery.getName();
                    if(csparqlStreamTable.containsKey(newStreamName)){
                        engine.unregisterStream(newStreamName);
                        csparqlStreamTable.remove(newStreamName);
                        engine.unregisterQuery(csparqlQuery.getQueryID());
                        csparqlQueryTable.remove(queryName);
                        getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
                        getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
                        getContext().getAttributes().put("csparqlengine", engine);
                        this.getResponse().setStatus(Status.SUCCESS_OK,"Query and stream " + newStreamName + " succesfully unregistered");
                        this.getResponse().setEntity(gson.toJson("Query and stream " + newStreamName + " succesfully unregistered"), MediaType.APPLICATION_JSON);
                    }
                } else {
                    engine.unregisterQuery(csparqlQuery.getQueryID());
                    csparqlQueryTable.remove(queryName);
                    getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
                    getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
                    getContext().getAttributes().put("csparqlengine", engine);
                    this.getResponse().setStatus(Status.SUCCESS_OK,queryName + " succesfully unregistered");
                    this.getResponse().setEntity(gson.toJson(queryName + " succesfully unregistered"), MediaType.APPLICATION_JSON);
                }
            } else {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,queryName + " ID is not associated to any registered query");
                this.getResponse().setEntity(gson.toJson(queryName + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
            }
        } catch (Exception e) {
            logger.error("Error while unregistering query" + queryName, e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while unregistering query" + queryName);
            this.getResponse().setEntity(gson.toJson("Error while unregistering query" + queryName), MediaType.APPLICATION_JSON);
        } finally{
            this.getResponse().commit();
            this.commit();
            this.release();
        }

    }

    @SuppressWarnings({ "unchecked" })
    @Post
    public void changeQueryStatus(Representation rep){

        String queryName = new String();

        try {

            Form f = new Form(rep);

            csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
            csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
            engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");
            String hostName = Config.getInstance().getHostName();
            String serverAddress = (String) getContext().getAttributes().get("complete_server_address");


            String origin = getRequest().getClientInfo().getAddress();
            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));


            queryName = (String) this.getRequest().getAttributes().get("queryname");
            String action = f.getFirstValue("action");

            if (action != null){
                if (csparqlQueryTable.containsKey(queryName)) {
                    Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
                    if (action.equals("addobserver")) {
                        String host = f.getFirstValue("host");
                        int port = Integer.parseInt(f.getFirstValue("port"));
                        //					String observerID = UUID.randomUUID().toString();
                        String observerID = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8");
                        Model sGraph = createsGraph("ws://" + host + ":" + port + "/" + csparqlQuery.getName() + "/results", null, null);
                        Csparql_Observer_Descriptor csObs = new Csparql_Observer_Descriptor(observerID, "websocket",
                                new Observer4WS(host, port, "/" + csparqlQuery.getName(), Config.getInstance().getSendEmptyResultsProperty()), sGraph);
                        csparqlQuery.addObserver(csObs);
                        this.getResponse().setStatus(Status.SUCCESS_OK, "Observer succesfully registered and now available at " +
                                serverAddress + " /queries/ " + csparqlQuery.getName() + "/observers/" + csObs.getId() + "\n" +
                                "The data is available at ws://" + host + ":" + port + "/" + csparqlQuery.getName() + "/results" + "\n" +
                                "The sGraph of the new stream is available at " + serverAddress + "/queries/"+ csparqlQuery.getName() + "/observers/" + csObs.getId());
                        this.getResponse().setEntity(gson.toJson("Observer succesfully registered and now available at " +
                                serverAddress + " /queries/ " + csparqlQuery.getName() + "/observers/" + csObs.getId() + "     " +
                                "The data is available at ws://" + host + ":" + port + "/" + csparqlQuery.getName() + "/results" + "   " +
                                "The sGraph of the new stream is available at " + serverAddress + "/queries/"+ csparqlQuery.getName() + "/observers/" + csObs.getId()), MediaType.APPLICATION_JSON);
                    } else if (action.equals("pause")) {
                        if (!csparqlQuery.getQueryStatus().equals(Rsp_services_Component_Status.PAUSED)) {
                            engine.stopQuery(csparqlQuery.getQueryID());
                            csparqlQuery.changeQueryStatus(Rsp_services_Component_Status.PAUSED);
                            csparqlQueryTable.put(queryName, csparqlQuery);
                            this.getResponse().setStatus(Status.SUCCESS_OK, queryName + " succesfully paused");
                            this.getResponse().setEntity(gson.toJson(queryName + " succesfully paused"), MediaType.APPLICATION_JSON);
                        } else {
                            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " is already paused");
                            this.getResponse().setEntity(gson.toJson(queryName + " is already paused"), MediaType.APPLICATION_JSON);
                        }
                    } else if (action.equals("restart")) {
                        if (!csparqlQuery.getQueryStatus().equals(Rsp_services_Component_Status.RUNNING)) {
                            engine.startQuery(csparqlQuery.getQueryID());
                            csparqlQuery.changeQueryStatus(Rsp_services_Component_Status.RUNNING);
                            csparqlQueryTable.put(queryName, csparqlQuery);
                            this.getResponse().setStatus(Status.SUCCESS_OK, queryName + " succesfully restarted");
                            this.getResponse().setEntity(gson.toJson(queryName + " succesfully restarted"), MediaType.APPLICATION_JSON);
                        } else {
                            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " is already running");
                            this.getResponse().setEntity(gson.toJson(queryName + " is already running"), MediaType.APPLICATION_JSON);
                        }
                    } else {
                        this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Problem with specified action. The action must be pause, restart or addobserver");
                        this.getResponse().setEntity(gson.toJson("Problem with specified action. The action must be pause, restart or addobserver"), MediaType.APPLICATION_JSON);
                    }
                    getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
                    getContext().getAttributes().put("csparqlengine", engine);
                } else {
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " ID is not associated to any registered query");
                    this.getResponse().setEntity(gson.toJson(queryName + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
                }
            } else{
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Please specify an action in the request body");
                this.getResponse().setEntity(gson.toJson("Please specify an action in the request body"), MediaType.APPLICATION_JSON);
            }
        } catch (Exception e) {
            logger.error("Error while changing query status", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error during query operations");
            this.getResponse().setEntity(gson.toJson("Error during query operations"), MediaType.APPLICATION_JSON);
        } finally{
            this.getResponse().commit();
            this.commit();
            this.release();
        }
    }

    private ArrayList<String> extractStreamNamesFromQuery(String query){
        String tempQuery = query;
        ArrayList<String> streamNameList = new ArrayList<String>();

        int index = tempQuery.indexOf("FROM STREAM ");

        while(index != -1){

            tempQuery = tempQuery.substring(index + 12, tempQuery.length());
            streamNameList.add(tempQuery.substring(tempQuery.indexOf("<") + 1, tempQuery.indexOf(">")));

            index = tempQuery.indexOf("FROM STREAM ");

        }

        return streamNameList;
    }

    private String extractNameFromQuery(String query){
        String tempQuery = query;

        int index = tempQuery.indexOf("REGISTER QUERY ");
        if(index != -1){
            return tempQuery.substring(index + 15, tempQuery.indexOf("AS") - 1);
        } else {
            index = tempQuery.indexOf("REGISTER STREAM ");
            return tempQuery.substring(index + 16, tempQuery.indexOf("AS") - 1);
        }
    }

    private String extractQueryType(String query){
        if(query.contains("REGISTER STREAM"))
            return "stream";
        else
            return "query";
    }

    private boolean checkInputStream(ArrayList<String> streamList){

        for(String stream : streamList){
            if(!csparqlStreamTable.containsKey(stream)){
                return false;
            }
        }
        return true;
    }

    private String rewriteQuery(String query, ArrayList<String> strList){
        for(String str : strList){
            query = query.replace("<" + str + ">", "<" + csparqlStreamTable.get(str).getIri() + ">");
        }
        return query;
    }

    private Model createsGraph(String streamLocation, String tBoxLocation, String aBoxLocation){
        Model model = ModelFactory.createDefaultModel();

        model.add(model.createResource("http://streamreasoning.org/ontologies/SLD4TripleWave#sGraph"),
                model.createProperty("http://streamreasoning.org/ontologies/SLD4TripleWave#streamLocation"),
                model.createResource(streamLocation));
        try {
            model.add(model.createResource("http://streamreasoning.org/ontologies/SLD4TripleWave#sGraph"),
                    model.createProperty("http://streamreasoning.org/ontologies/SLD4TripleWave#lastUpdated"),
                    model.createTypedLiteral(longToDate(System.currentTimeMillis()), XSDDatatype.XSDdateTime));
        } catch (Exception e) {
            model.add(model.createResource("http://streamreasoning.org/ontologies/SLD4TripleWave#sGraph"),
                    model.createProperty("http://streamreasoning.org/ontologies/SLD4TripleWave#lastUpdated"),
                    model.createTypedLiteral("2000-01-01T00:00:00.000Z", XSDDatatype.XSDdateTime));
        }
        if(tBoxLocation != null)
            model.add(model.createResource("http://streamreasoning.org/ontologies/SLD4TripleWave#sGraph"),
                    model.createProperty("http://streamreasoning.org/ontologies/SLD4TripleWave#tBoxLocation"),
                    model.createResource(tBoxLocation));
        if(aBoxLocation != null)
            model.add(model.createResource("http://streamreasoning.org/ontologies/SLD4TripleWave#sGraph"),
                    model.createProperty("http://streamreasoning.org/ontologies/SLD4TripleWave#aBoxLocation"),
                    model.createResource(aBoxLocation));
        return model;
    }

    public String longToDate(final long date) throws Exception{
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar).toXMLFormat();
    }
}
