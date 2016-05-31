package it.polimi.deib.rsp_services_csparql.observers.utilities;

/**
 * Created by Marco Balduini on 31/05/16 as part of project rsp-services-api.
 */
public class ObserverDescriptorForGet {
    private String id;
    private String type;
    private String sGraphURL;

    public ObserverDescriptorForGet(String id, String type, String sGraphURL) {
        this.id = id;
        this.type = type;
        this.sGraphURL = sGraphURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getsGraphURL() {
        return sGraphURL;
    }

    public void setsGraphURL(String sGraphURL) {
        this.sGraphURL = sGraphURL;
    }
}
