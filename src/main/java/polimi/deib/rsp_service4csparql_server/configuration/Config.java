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
