package com.jorge.test;

import java.util.Properties;

public class ApplicationProperties extends Properties {
    
    private static final long serialVersionUID = 1L;
    
    public static Properties properties = new Properties();
    
    public ApplicationProperties() {
        if (properties != null) {
        	putAll(properties);
        }
    }

}
