package de.predic8.vacation;

import java.util.HashMap;

import static org.apache.camel.Exchange.*;
import org.apache.camel.builder.RouteBuilder;
import org.jolokia.jvmagent.JolokiaServer;
import org.jolokia.jvmagent.JolokiaServerConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;

import com.sun.xml.internal.xsom.impl.scd.Iterators.Map;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("jetty:http://localhost:8081/vacation/")
        			.routeId("beach-or-ski")
                .unmarshal( "xmljson")
                .setHeader( "city", xpath("//city/text()"))
                .setHeader( "name", xpath("//name/text()"))
                .log( "${header.name} wants to go to ${header.city}")
                .setHeader( HTTP_PATH, simple("data/2.5/find?q=${header.city}&units=metric&mode=xml"))
                .setHeader( HTTP_METHOD, constant("GET"))
                .to( "jetty:http://api.openweathermap.org/?bridgeEndpoint=true")
                .convertBodyTo( Document.class)
                .setHeader( "count", xpath("//count/text()", String.class))
                .to( "xslt:weather2booking.xsl")
                .choice()
	                .when( header( "count").isNotEqualTo( "1"))
	                    .setHeader( HTTP_RESPONSE_CODE, constant("404"))
	                    .setBody().simple(" { \"message\" : \"city not found\" }")
	                    .endChoice()
	                .when( xpath( "//temp > 20"))
	                    .inOnly( "activemq:beach")
	                    .setBody().simple( "{ \"message\" : \"Enjoy the sun!\" }")
	                    .endChoice()
	                .otherwise()
	                    .inOnly( "activemq:ski")
	                    .setBody().simple( "{ \"message\" : \"You go skiing!\" }")
	                    .endChoice()	
	                .end();	
        


    }

    @SuppressWarnings("all")
    public static void main(String[] args) throws Exception {

    	HashMap map = new HashMap();
    	map.put("port", "8181");
    	JolokiaServerConfig config = new JolokiaServerConfig(map);
    	JolokiaServer server = new JolokiaServer(config, true);
    	server.start();
    	
        new ClassPathXmlApplicationContext("camel.xml");

    }

}
