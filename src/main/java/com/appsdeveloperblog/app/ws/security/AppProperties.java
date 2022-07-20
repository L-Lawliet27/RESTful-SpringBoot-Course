package com.appsdeveloperblog.app.ws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

    @Autowired
    private Environment env;

    //This gets the value from the application.properties file that we need, in this case the tokenSecret
    //this will be used in the encryption of the access token
    public String getTokenSecret(){
        return env.getProperty("tokenSecret");
    }

}
