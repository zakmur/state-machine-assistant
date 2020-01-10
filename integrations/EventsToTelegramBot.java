import java.util.Map;
import java.util.HashMap;
import java.util.Base64;

import org.apache.camel.Exchange;

import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;

import org.apache.camel.model.dataformat.JsonLibrary;

public class EventsToTelegramBot extends RouteBuilder {
  @Override
  public void configure() throws Exception {
    
    from("kafka:{{kafka.from.topic}}?brokers={{kafka.bootstrap-servers}}&groupId={{kafka.groupId}}")
        .routeId("events-to-bot")
        .onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "Error connecting to server, please check the application.properties file ${exception.message}")
            .end()
        .log("Route started from Kafka Topic {{kafka.from.topic}}")
        .log("body: ${body}")
        .log("Sending message to telegram bot http://{{telegram-bot.host}}:{{telegram-bot.port}}/new-message: ${body}")
        .to("direct:send-event-to-bot")
        .log("Event sent successfully: ${body}");

    from("direct:send-event-to-bot")
        .routeId("send-event-to-bot")
        //.removeHeaders("*") // Otherwise you'll probably get a 400 error
        .setHeader("id", header(Exchange.TIMER_COUNTER))
        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
        .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
        .setHeader(Exchange.HTTP_CHARACTER_ENCODING, constant("UTF-8"))
        .log("Executing saga #${headers} ${body}")
        .to("http://{{telegram-bot.host}}:{{telegram-bot.port}}/new-message")
        .log("Patient info sent successfully: ${body}");
  }
}