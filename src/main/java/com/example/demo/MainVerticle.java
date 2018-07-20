package com.example.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class MainVerticle extends AbstractVerticle {
    private Person[] testing = new Person[10];

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);  
        router.route().handler(BodyHandler.create());
        testing[0] = new Person("Mike", 35);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Access-Control-Allow-Method");
        allowedHeaders.add("Access-Control-Allow-Credentials");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        /*
         * these methods aren't necessary for this sample, 
         * but you may need them for your projects
         */
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

        // you will need to allow outbound and inbound to allow eventbus communication.
        BridgeOptions opts = new BridgeOptions();
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);
        opts.addInboundPermitted(new PermittedOptions().setAddress("client-to-server"));
        opts.addOutboundPermitted(new PermittedOptions().setAddress("client-to-server"));
        router.route().handler(StaticHandler.create());

        // Serve the static resources
        router.route().handler(StaticHandler.create());

        router.get("/person").handler(req -> {
            req.response()
            .putHeader("content-type", "application/json")
            .end(Json.encode(testing));
        });

        router.post("/post").handler(this::addOne);

        EventBus eb = vertx.eventBus();

        // eb.consumer("client-to-server", message -> {
        //     System.out.println(message.body());

        //     message.reply(message.body());
        // });

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8080);
        System.out.println("HTTP server started on port 8080");
    }

    private void addOne(RoutingContext ctx) {
        final DeliveryOptions opts = new DeliveryOptions().setSendTimeout(2000);
        Person newPerson = Json.decodeValue(ctx.getBodyAsString(), Person.class);
        testing[1] = newPerson;
        vertx.eventBus().send("client-to-server", ctx.getBodyAsJson(), opts);
        ctx.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(testing));
    }
}

class Person {
    private String name;
    private int age; 

    public Person() {
        this.name = "";
        this.age = 0;
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
