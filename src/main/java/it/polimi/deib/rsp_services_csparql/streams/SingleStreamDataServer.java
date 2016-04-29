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
package it.polimi.deib.rsp_services_csparql.streams;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_Engine;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_RDF_Stream;
import it.polimi.deib.rsp_services_csparql.commons.Utilities;
import it.polimi.deib.rsp_services_csparql.streams.utilities.CsparqlStreamDescriptionForGet;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
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


import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

import javax.websocket.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.transform.Source;

public class SingleStreamDataServer extends ServerResource {

    private static Hashtable<String, Csparql_RDF_Stream> csparqlStreamTable;
    private Csparql_Engine engine;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private Logger logger = LoggerFactory.getLogger(SingleStreamDataServer.class.getName());

    private int i = 0;
    private int j = 0;

    private String queryString = "PREFIX sld: <http://streamreasoning.org/ontologies/SLD4TripleWave#> " +
            "SELECT ?wsurl ?tboxurl ?aboxurl " +
            "WHERE {" +
            "?sGraph sld:streamLocation ?wsurl ; " +
            "sld:tBoxLocation ?tboxurl . " +
            "OPTIONAL { ?sGraph sld::staticaBoxLoxation ?aboxurl . } " +
            "}";
    private Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

    @SuppressWarnings("unchecked")
    @Options
    public void optionsRequestHandler(){
        ClientInfo c = getRequest().getClientInfo();
        String origin = c.getAddress();
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<Header>(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
        responseHeaders.add(new Header("Access-Control-Allow-Methods", "PUT,POST,DELETE"));

    }

    @SuppressWarnings({ "unchecked" })
    @Get
    public void getStreamsInformations(){

        try{

            String origin = getRequest().getClientInfo().getAddress();
            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

            csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
            ArrayList<CsparqlStreamDescriptionForGet> streamDescriptionList = new ArrayList<CsparqlStreamDescriptionForGet>();

            Set<String> keySet = csparqlStreamTable.keySet();
            Csparql_RDF_Stream registeredCsparqlStream;
            for(String key : keySet){
                registeredCsparqlStream = csparqlStreamTable.get(key);
                streamDescriptionList.add(new CsparqlStreamDescriptionForGet(registeredCsparqlStream.getStream().getIRI(), registeredCsparqlStream.getStatus()));
            }

            this.getResponse().setStatus(Status.SUCCESS_OK,"Information about streams succesfully extracted");
            this.getResponse().setEntity(gson.toJson(streamDescriptionList), MediaType.APPLICATION_JSON);

        } catch(Exception e){
            logger.error("Error while getting multiple streams informations", e);
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
    public void registerStream(Representation rep){

        try{

            Model sGraph = ModelFactory.createDefaultModel();
            Model tBox = ModelFactory.createDefaultModel();
            Model aBox = ModelFactory.createDefaultModel();

            String origin = getRequest().getClientInfo().getAddress();
            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

            csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
            engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

            String inputStreamName = null;

            try {
                Form f = new Form(rep);

                inputStreamName = URLDecoder.decode(f.getFirstValue("streamIri"), "UTF-8");
                sGraph = RDFDataMgr.loadModel(inputStreamName, Lang.JSONLD);
                sGraph.write(System.out);
            } catch (Exception e){
                logger.error(e.getMessage(), e);
            }

            QueryExecution qexec = QueryExecutionFactory.create(query, sGraph);

            ResultSet rs = qexec.execSelect();

            String wsUrl = new String();
            String tBoxUrl = new String();
            String aBoxUrl = new String();

            while(rs.hasNext()){
                QuerySolution qs = rs.next();
                if(qs.get("wsurl").isLiteral())
                    wsUrl = qs.getLiteral("wsurl").getLexicalForm();
                else
                    wsUrl = qs.get("wsurl").toString();
                if(qs.get("tboxurl").isLiteral())
                    tBoxUrl = qs.getLiteral("tboxurl").getLexicalForm();
                else
                    tBoxUrl = qs.get("tboxurl").toString();
                if(qs.contains("aboxurl"))
                    if(qs.get("aboxurl").isLiteral())
                        aBoxUrl = qs.getLiteral("aboxurl").getLexicalForm();
                    else
                        aBoxUrl = qs.get("aboxurl").toString();
            }

            if(tBoxUrl != null && !tBoxUrl.isEmpty())
                try {
                    tBox = RDFDataMgr.loadModel(tBoxUrl, Lang.JSONLD);
                } catch (Exception e) {
                    tBox = RDFDataMgr.loadModel(tBoxUrl, Lang.RDFXML);
                }
            if(aBoxUrl != null && !aBoxUrl.isEmpty())
                try {
                    aBox = RDFDataMgr.loadModel(aBoxUrl, Lang.JSONLD);
                } catch (Exception e) {
                    aBox = RDFDataMgr.loadModel(aBoxUrl, Lang.RDFXML);
                }
            //Aggiungere pezzo con collegamento al ws

            if(!csparqlStreamTable.containsKey(inputStreamName)){
                RdfStream stream = new RdfStream(inputStreamName);
                Csparql_RDF_Stream csparqlStream = new Csparql_RDF_Stream(stream, Rsp_services_Component_Status.RUNNING);
                if(tBox != null && !tBox.isEmpty())
                    csparqlStream.settBox(tBox);
                if(aBox != null && !aBox.isEmpty())
                    csparqlStream.setStaticAbox(aBox);
                if(wsUrl != null)
                    csparqlStream.setSourceURI(wsUrl);

                Session session = connectToWS(csparqlStream);
                if(session != null)
                    csparqlStream.setWsSession(session);

                csparqlStreamTable.put(inputStreamName, csparqlStream);
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
            logger.error(e.getMessage(), e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            this.getResponse().setEntity(gson.toJson(e.getMessage()), MediaType.APPLICATION_JSON);
        } finally{
            this.getResponse().commit();
            this.commit();
            this.release();
        }

    }

//    @SuppressWarnings({ "unchecked" })
//    @Delete
//    public void unregisterStream(Representation rep){
//        try{
//
//            String origin = getRequest().getClientInfo().getAddress();
//            Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
//            if (responseHeaders == null) {
//                responseHeaders = new Series<Header>(Header.class);
//                getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
//            }
//            responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
//
//            csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
//            engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");
//
//            String inputStreamName;
//
//            Form f = new Form(rep);
//            inputStreamName = URLDecoder.decode(f.getFirstValue("streamIri"), "UTF-8");
//
//            if(csparqlStreamTable.containsKey(inputStreamName)){
//                RdfStream stream = csparqlStreamTable.get(inputStreamName).getStream();
//                engine.unregisterStream(stream.getIRI());
//                csparqlStreamTable.remove(inputStreamName);
//                getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
//                getContext().getAttributes().put("csparqlengine", engine);
//                this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully unregistered");
//                this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully unregistered"), MediaType.APPLICATION_JSON);
//            } else {
//                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " does not exist");
//                this.getResponse().setEntity(gson.toJson(inputStreamName + " does not exist"), MediaType.APPLICATION_JSON);
//            }
//
//        } catch(Exception e){
//            logger.error("Error while unregistering stream", e);
//            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,Utilities.getStackTrace(e));
//            this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
//        } finally{
//            this.getResponse().commit();
//            this.commit();
//            this.release();
//        }
//    }

    @SuppressWarnings({ "unchecked" })
    @Post
    public void feedStream(Representation rep) {

        try {

            Form f = new Form(rep);
            if (f.getFirstValue("action") != null && f.getFirstValue("action").equalsIgnoreCase("DELETE")) {

                String origin = getRequest().getClientInfo().getAddress();
                Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
                if (responseHeaders == null) {
                    responseHeaders = new Series<Header>(Header.class);
                    getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
                }
                responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

                csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
                engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

                String inputStreamName = URLDecoder.decode(f.getFirstValue("streamIri"), "UTF-8");

                if (csparqlStreamTable.containsKey(inputStreamName)) {
                    Csparql_RDF_Stream csStream = csparqlStreamTable.get(inputStreamName);
                    csStream.getWsSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,inputStreamName + " unregistered from engine!!"));
                    engine.unregisterStream(csStream.getStream().getIRI());
                    csparqlStreamTable.remove(inputStreamName);
                    getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
                    getContext().getAttributes().put("csparqlengine", engine);
                    this.getResponse().setStatus(Status.SUCCESS_OK, "Stream " + inputStreamName + " succesfully unregistered");
                    this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully unregistered"), MediaType.APPLICATION_JSON);
                } else {
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, inputStreamName + " does not exist");
                    this.getResponse().setEntity(gson.toJson(inputStreamName + " does not exist"), MediaType.APPLICATION_JSON);
                }

            } else {

                String origin = getRequest().getClientInfo().getAddress();
                Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
                if (responseHeaders == null) {
                    responseHeaders = new Series<Header>(Header.class);
                    getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
                }
                responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

                csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
                engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");


                String inputStreamName = URLDecoder.decode(f.getFirstValue("streamIri"), "UTF-8");

                if (csparqlStreamTable.containsKey(inputStreamName)) {
                    Csparql_RDF_Stream streamRepresentation = csparqlStreamTable.get(inputStreamName);

                    String jsonSerialization = f.getFirstValue("payload");

                    Model model = ModelFactory.createDefaultModel();

                    try {
                        model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")), null, "RDF/JSON");
                        long ts = System.currentTimeMillis();

                        StmtIterator it = model.listStatements();
                        while (it.hasNext()) {
                            Statement st = it.next();
                            streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
                        }

                        this.getResponse().setStatus(Status.SUCCESS_OK, "Stream " + inputStreamName + " succesfully feeded");
                        this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
                    } catch (Exception e) {
                        try {
                            model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")), null, "RDF/XML");
                            long ts = System.currentTimeMillis();

                            StmtIterator it = model.listStatements();
                            while (it.hasNext()) {
                                Statement st = it.next();
                                streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
                            }

                            this.getResponse().setStatus(Status.SUCCESS_OK, "Stream " + inputStreamName + " succesfully feeded");
                            this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
                        } catch (Exception e1) {
                            try {
                                model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")), null, "N-TRIPLE");
                                long ts = System.currentTimeMillis();

                                StmtIterator it = model.listStatements();
                                while (it.hasNext()) {
                                    Statement st = it.next();
                                    streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
                                }

                                this.getResponse().setStatus(Status.SUCCESS_OK, "Stream " + inputStreamName + " succesfully feeded");
                                this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
                            } catch (Exception e2) {
                                model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")), null, "TURTLE");
                                long ts = System.currentTimeMillis();

                                StmtIterator it = model.listStatements();
                                while (it.hasNext()) {
                                    Statement st = it.next();
                                    streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
                                }

                                this.getResponse().setStatus(Status.SUCCESS_OK, "Stream " + inputStreamName + " succesfully feeded");
                                this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
                            }
                        }
                    }

                } else {
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Specified stream does not exists");
                    this.getResponse().setEntity(gson.toJson("Specified stream does not exists"), MediaType.APPLICATION_JSON);
                }
            }

        } catch (Exception e) {
            logger.error("Error while changing status of a stream", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, Utilities.getStackTrace(e));
            this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
        } finally {
            this.getResponse().commit();
            this.commit();
            this.release();
        }
    }

    public Session connectToWS(Csparql_RDF_Stream stream) {

        final Csparql_RDF_Stream str = stream;

        ClientManager client = ClientManager.createClient();
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        Session session;

        ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

            @Override
            public boolean onDisconnect(CloseReason closeReason) {

                if(closeReason.getCloseCode().equals(CloseReason.CloseCodes.NORMAL_CLOSURE)){
                    logger.info("Disconnection Handler - Connection Normally Closed \n {}", closeReason.getReasonPhrase());
                    System.out.println("Disconnection Handler - Connection Normally Closed \n" + closeReason.getReasonPhrase());

                    return false;
                } else {

                    if (i < 10) {
                        i++;
                        j++;
                        try {
                            logger.info("Disconnection Handler - Connection failed");
                            System.out.println(longToDate(System.currentTimeMillis()) + " - Disconnection Handler - Connection failed");
                            Thread.sleep(i * i * 1000);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            return true;
                        }

                    } else {
                        j++;
                        try {
                            logger.info("Disconnection Handler - Connection failed");
                            System.out.println(longToDate(System.currentTimeMillis()) + " - Disconnection Handler - Connection failed");
                            Thread.sleep(i * i * 1000);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            return true;
                        }
                    }

                    return true;
                }
            }

            @Override
            public boolean onConnectFailure(Exception exception) {

                if (i < 10) {
                    i++;
                    j++;
                    try {
                        logger.info("Disconnection Handler - Connection failed");
                        System.out.println(longToDate(System.currentTimeMillis()) + " - Disconnection Handler - Connection failed");
                        Thread.sleep(i * i * 1000);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return true;
                    }

                } else {
                    j++;
                    try {
                        logger.info("Disconnection Handler - Connection failed");
                        System.out.println(longToDate(System.currentTimeMillis()) + " - Disconnection Handler - Connection failed");
                        Thread.sleep(i * i * 1000);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return true;
                    }
                }
                return true;
            }

            @Override
            public long getDelay() {
                return 1;
            }
        };

        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
        client.setDefaultMaxSessionIdleTimeout(0);

        //		lastConnectionTS = System.currentTimeMillis();
        try {
            //			messageLatch = new CountDownLatch(1);
            session = client.connectToServer(new Endpoint() {
                private Logger logger = LoggerFactory.getLogger(this.getClass());

                @Override
                public void onOpen(final Session session, EndpointConfig EndpointConfig) {

                    //					messageCount = 0;
                    logger.info("Client connected to {}", session.getRequestURI().toString());
                    System.out.println("Client connected to " + session.getRequestURI().toString());

                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            try {
                                Model model = deserializizeAsJsonSerialization(message, null);
                                str.feed_RDF_stream(model);
                                System.out.println(model.size() + " triples streamed so far");
                                logger.info("{} triples streamed so far", model.size());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    });
                }

                @Override
                public void onClose(Session session, CloseReason reason){
                    System.out.println("Closing Session " + session.getId() + " because of " +  reason.getReasonPhrase());
                    logger.info("Closing Session {} because of {}", session.getId(), reason.getReasonPhrase());
                }

                @Override
                public void onError(Session session, Throwable t) {
                    logger.error(t.getMessage(), t);
                }
            }, cec, new URI(str.getSourceURI()));
            i = 0;
            j = 0;
            logger.info("Connected!!\nSession id: {}", session.getId());
            System.out.println("Connected!!\nSession id: " + session.getId());
            //			messageLatch.await();

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
        return session;

    }

    private static String longToDate(final long date) throws Exception{
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar).toXMLFormat();
    }

    public static Model deserializizeAsJsonSerialization(String asJsonSerialization, JsonLdOptions options){

//		System.out.println(asJsonSerialization);
//		logger.info("Input string {}", asJsonSerialization);

        Model model = ModelFactory.createDefaultModel();
        try {
            Object jsonObject = null;
            RDFDataset rd = null;

            try {
                jsonObject = JsonUtils.fromString(asJsonSerialization);

                if (options != null)
                    rd = (RDFDataset) JsonLdProcessor.toRDF(jsonObject, options);
                else
                    rd = (RDFDataset) JsonLdProcessor.toRDF(jsonObject);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Set<String> graphNames = rd.graphNames();

            for (String graphName : graphNames){

                List<RDFDataset.Quad> l = rd.getQuads(graphName);

                ResourceImpl subject;
                PropertyImpl predicate;
                ResourceImpl object;

                for (com.github.jsonldjava.core.RDFDataset.Quad q : l) {
                    if (q.getSubject().isBlankNode()) {
                        AnonId aid = new AnonId(q.getSubject().getValue());
                        subject = new ResourceImpl(aid);
                    } else {
                        subject = new ResourceImpl(q.getSubject().getValue());
                    }

                    predicate = new PropertyImpl(q.getPredicate().getValue());

                    if (!q.getObject().isLiteral()) {
                        if (q.getObject().isBlankNode()) {
                            AnonId aid = new AnonId(q.getObject().getValue());
                            object = new ResourceImpl(aid);
                        } else {
                            object = new ResourceImpl(q.getObject().getValue());
                        }
                        model.add(subject, predicate, object);
                    } else {
                        model.add(subject, predicate, model.createLiteral(q.getObject().getValue(), q.getObject().getDatatype()));
                    }
                }
            }
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return ModelFactory.createDefaultModel();
        }
    }
}
