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
package polimi.deib.rsp_service4csparql_server.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {
	private static Config _instance = null;
	private static final Logger logger = LoggerFactory.getLogger(Config.class); 
	
	private Configuration config;
	
	private Config(){
		try {
			config = new PropertiesConfiguration("setup.properties");
		} catch (ConfigurationException e) {
			logger.error("Error while reading the configuration file", e);
		}
	}
	
	public static Config getInstance(){
		if(_instance==null)
			_instance=new Config();
		return _instance;
	}
	
	public String getInferenceRulesFile(){
		return config.getString("csparql_engine.inference_rule_file");
	}
	
	public boolean getActivateInference(){
		return config.getBoolean("csparql_engine.activate_inference");
	}
	
	public String getServerVersion(){
		return config.getString("csparql_server.version");
	}
	
	public int getServerPort(){
		return config.getInt("csparql_server.port");
	}
	
	public String getStreamBaseUri(){
		if(config.getString("csparql_stream.stream.base_uri").endsWith("/"))
			return config.getString("csparql_stream.stream.base_uri");
		else
			return config.getString("csparql_stream.stream.base_uri") + "/";
	}
				
	public boolean getEnableTSFunction(){
		return config.getBoolean("csparql_engine.enable_timestamp_function");
	}
	
	public boolean getSendEmptyResultsProperty(){
		return config.getBoolean("csparql_engine.send_empty_results");
	}	
}
