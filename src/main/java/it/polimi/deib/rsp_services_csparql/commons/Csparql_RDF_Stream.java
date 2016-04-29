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
package it.polimi.deib.rsp_services_csparql.commons;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.streamreasoning.rsp_services.commons.Rsp_services_Component_Status;
import org.streamreasoning.rsp_services.interfaces.RDF_Stream_Interface;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

import javax.websocket.Session;

public class Csparql_RDF_Stream implements RDF_Stream_Interface{

	private RdfStream stream;
	private Rsp_services_Component_Status status;
    private Model tBox;
    private Model staticAbox;
    private String sourceURI;
    private Session wsSession;
	
	public Csparql_RDF_Stream() {
		super();
	}

    public Csparql_RDF_Stream(RdfStream stream, Rsp_services_Component_Status status) {
        this.stream = stream;
        this.status = status;
    }

    public RdfStream getStream() {
		return stream;
	}
	
	public void setStream(RdfStream stream) {
		this.stream = stream;
	}
	
	public Rsp_services_Component_Status getStatus() {
		return status;
	}
	
	public void setStatus(Rsp_services_Component_Status status) {
		this.status = status;
	}

    public Model gettBox() {
        return tBox;
    }

    public void settBox(Model tBox) {
        this.tBox = tBox;
    }

    public Model getStaticAbox() {
        return staticAbox;
    }

    public void setStaticAbox(Model staticAbox) {
        this.staticAbox = staticAbox;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public Session getWsSession() {
        return wsSession;
    }

    public void setWsSession(Session wsSession) {
        this.wsSession = wsSession;
    }

    @Override
	public void feed_RDF_stream(Object dataSerialization) {
		RdfQuadruple quadruple = (RdfQuadruple) dataSerialization;
		stream.put(quadruple);
	}

    public void feed_RDF_stream(Model model) {

        RdfQuadruple quadruple;
        long ts = System.currentTimeMillis();

        StmtIterator it = model.listStatements();
        while(it.hasNext()){
            Statement st = it.next();
            stream.put(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
        }
    }
	
}
