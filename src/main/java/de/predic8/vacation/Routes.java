package de.predic8.vacation;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;

/**
 * 
 *      Input JSON
 *       {
 *          "name": "Thomas",
 *          "city": "Bonn"
 *      }  
 *
 */
public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("jetty:http://localhost:8081/weathermap/")
                .unmarshal( "xmljson")
                .setHeader( "city", xpath("//city/text()"))
                .setHeader( "name", xpath("//name/text()"))
                .log( "${header.name} wants to go to ${header.city}")
                .setHeader( Exchange.HTTP_PATH, simple("data/2.5/find?q=${header.city}&units=metric&mode=xml"))
                .setHeader( Exchange.HTTP_METHOD, constant("GET"))
                .to( "jetty:http://api.openweathermap.org/?bridgeEndpoint=true")
                .convertBodyTo( Document.class)
                .setHeader( "count", xpath("//count/text()", String.class))
                .setHeader( "temp", xpath("//temperature/@value", String.class))
                .to( "xslt:weather2booking.xsl")
                .choice()
	                .when( header( "count").isNotEqualTo( "1"))
	                    .setHeader( Exchange.HTTP_RESPONSE_CODE, constant("400"))
	                    .setBody().simple(" { \"message\" : \"city not found\" }")
	                    .endChoice()
	                .when( simple( "${header.temp} > 20"))
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

        new ClassPathXmlApplicationContext("camel.xml");

    }

}
