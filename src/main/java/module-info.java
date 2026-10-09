module NovaClinic {

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires jdk.jsobject;

    requires java.sql;
    requires java.mail;
    requires twilio;
    requires java.net.http;
    requires org.json;
    requires java.desktop;
    requires opencv;

    requires com.github.librepdf.openpdf;

    // Google OAuth
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.json.gson;
    requires google.api.services.oauth2.v2.rev157;
    requires google.api.client;

    opens controllers to javafx.fxml;
    opens entities to javafx.base;
    opens test to javafx.graphics;
}