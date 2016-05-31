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
package it.polimi.deib.rsp_services_csparql.observers.utilities.websocket;

import eu.larkc.csparql.common.RDFTable;
import org.glassfish.tyrus.server.Server;
import org.streamreasoning.rsp_services.interfaces.Continuous_Query_Observer_Interface;

import java.util.Observable;

public class Observer4WS implements Continuous_Query_Observer_Interface{


    private boolean sendEmptyResults;

    private String host;
    private int port;
    private String path;
    private String dataPath;

    public Observer4WS(String host, int port, String path, boolean sendEmptyResults) throws Exception {
        super();
        this.sendEmptyResults = sendEmptyResults;
        this.host = host;
        this.port = port;
        this.path = path;

        if(!path.startsWith("/"))
            path = "/" + path;

        dataPath = "ws://" + host + ":" + port + path + "/results";
        setUpWSServer();
    }

    @Override
    public void update(Observable o, Object arg) {

        RDFTable q = (RDFTable) arg;

        String jsonSerialization = q.getJsonSerialization();
        if (!sendEmptyResults) {
            if (!isEmptyResult(jsonSerialization)) {
                sendMessage(jsonSerialization);
            }
        } else {
            sendMessage(jsonSerialization);
        }
    }

    private void setUpWSServer() throws Exception{
        Server server = new Server(host, port, path, null, ResultsEndpoint.class);
        server.start();
    }

    private void sendMessage(String s){
        if(ResultsEndpoint.connectedSession.containsKey(this.path.replace("/","")))
            for (javax.websocket.Session session : ResultsEndpoint.connectedSession.get(this.path.replace("/",""))) {
                session.getAsyncRemote().sendText(s);
            }
    }

    private boolean isEmptyResult(String json) {
        return json.matches("\\{\\s*\\}") || json.matches("[\\s\\S]*\"bindings\"\\s*:\\s*\\[\\s*\\][\\s\\S]*");
    }

    @Override
    public String toString() {
        return dataPath;
    }
}
