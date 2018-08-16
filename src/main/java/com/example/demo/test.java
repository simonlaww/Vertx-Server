//package com.example.demo;
//
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.json.Json;
//import io.vertx.core.json.JsonObject;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.ext.web.Router;
//import io.vertx.ext.web.RoutingContext;
//import io.vertx.ext.web.handler.BodyHandler;
//import io.vertx.core.http.HttpServerResponse;
//import io.vertx.core.eventbus.DeliveryOptions;
//import io.vertx.core.eventbus.EventBus;
//import io.vertx.core.VertxOptions;
//
//import io.vertx.ext.web.handler.CorsHandler;
//import io.vertx.ext.web.handler.StaticHandler;
//import io.vertx.core.MultiMap;
//import io.vertx.core.http.HttpMethod;
//import java.util.Map;
//import java.util.HashSet;
//import java.util.Set;
//
//import io.vertx.ext.web.handler.sockjs.BridgeOptions;
//import io.vertx.ext.web.handler.sockjs.PermittedOptions;
//import io.vertx.ext.web.handler.sockjs.SockJSHandler;
//
//import io.vertx.ext.web.client.HttpResponse;
//import io.vertx.ext.web.client.HttpRequest;
//import io.vertx.ext.web.client.WebClient;
//import io.vertx.ext.web.client.WebClientOptions;
//
//public class MainVerticle extends AbstractVerticle {
//    private Person[] testing = new Person[10];
//
//    @Override
//    public void start() throws Exception {
//        Router router = Router.router(vertx);
//        router.route().handler(BodyHandler.create());
//        testing[0] = new Person("Mike", 35);
//
//        Set<String> allowedHeaders = new HashSet<>();
//        allowedHeaders.add("x-requested-with");
//        allowedHeaders.add("Access-Control-Allow-Origin");
//        allowedHeaders.add("Access-Control-Allow-Method");
//        allowedHeaders.add("Access-Control-Allow-Credentials");
//        allowedHeaders.add("origin");
//        allowedHeaders.add("Content-Type");
//        allowedHeaders.add("accept");
//        allowedHeaders.add("X-PINGARUNER");
//
//        Set<HttpMethod> allowedMethods = new HashSet<>();
//        allowedMethods.add(HttpMethod.GET);
//        allowedMethods.add(HttpMethod.POST);
//        allowedMethods.add(HttpMethod.OPTIONS);
//        /*
//         * these methods aren't necessary for this sample,
//         * but you may need them for your projects
//         */
//        allowedMethods.add(HttpMethod.DELETE);
//        allowedMethods.add(HttpMethod.PATCH);
//        allowedMethods.add(HttpMethod.PUT);
//
//        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
//
//        // you will need to allow outbound and inbound to allow eventbus communication.
//        BridgeOptions opts = new BridgeOptions();
//        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
//        router.route("/eventbus/*").handler(ebHandler);
//        opts.addInboundPermitted(new PermittedOptions().setAddress("client-to-server"));
//        opts.addOutboundPermitted(new PermittedOptions().setAddress("client-to-server"));
//        router.route().handler(StaticHandler.create());
//
//        // Serve the static resources
//        router.route().handler(StaticHandler.create());
//
//        router.get("/person").handler(req -> {
//            req.response()
//            .putHeader("content-type", "application/json")
//            .end(Json.encode(testing));
//        });
//
//        router.post("/post").handler(this::addOne);
//
//        VertxOptions vx = new VertxOptions();
//        // vx.setClusterPort(8080);
//        EventBus eb = vertx.eventBus();
//
//        eb.consumer("client-to-server", message -> {
//            System.out.println(message.body());
//
//            // message.reply(message.body());
//        });
//
//        WebClient client = WebClient.create(vertx,
//            new WebClientOptions()
//                .setSsl(true)
//                .setTrustAll(true)
//                .setVerifyHost(true)
//                .setDefaultPort(443)
//                .setKeepAlive(true)
//        );
//
//        router.get("/serviceTesting").handler(req -> {
//            int benchmark = 1;
//            long startTime = System.currentTimeMillis();
//
//            String token = "Intuit_IAM_Authentication intuit_token_type=IAM-Ticket,intuit_token=V1-111-a3w0wikm0k7dvz0m8ckr2b,intuit_userid=123145886102647,intuit_appid=Intuit.platform.fdp.docservice.test,intuit_app_secret=WRRjQAEafjaL49WTMSrmhe";
//            HttpRequest<Buffer> request = client
//                .get("financialdocument-e2e.platform.intuit.net", "/v2/documents?offset=0&ownership=owned&sharerType=USER&includeLocatorInfo=true&dedupeRequired=false")
//                .putHeader("Accept", "application/json")
//                .putHeader("Authorization", token);
//            for (int k = 0; k < 50; k++) {
//                request.send(ar -> {
//                    if (ar.succeeded()) {
//                        HttpResponse<Buffer> response = ar.result();
//                        System.out.println(response.bodyAsString().substring(0,2));
//                        System.out.println(System.currentTimeMillis() - startTime);
//                    } else {
//                        System.out.println("Something went wrong " + ar.cause().getMessage());
//                    }
//                });
//            }
//
//            System.out.println("hihi");
//
//            // client
//            // .put("financialdocument-prf.platform.intuit.net", "/v2/realms/9999")
//            // .putHeader("Content-Type", "application/json")
//            // .send(ar -> {
//            //     if (ar.succeeded()) {
//            //         HttpResponse<Buffer> response = ar.result();
//            //         System.out.println("Received response with status code");
//            //         System.out.println(response.statusCode());
//            //     } else {
//            //         System.out.println("Something went wrong " + ar.cause().getMessage());
//            //     }
//            // });
//
//            req.response()
//            .putHeader("content-type", "application/json")
//            .end(Json.encode(benchmark));
//        });
//
//        vertx.createHttpServer()
//            .requestHandler(router::accept)
//            .listen(8081);
//        System.out.println("HTTP server started on port 8081");
//    }
//
//    private void addOne(RoutingContext ctx) {
//        final DeliveryOptions opts = new DeliveryOptions().setSendTimeout(2000);
//        Person newPerson = Json.decodeValue(ctx.getBodyAsString(), Person.class);
//        testing[1] = newPerson;
//        vertx.eventBus().send("client-to-server", ctx.getBodyAsJson(), opts);
//        ctx.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(testing));
//    }
//}
//
//class Person {
//    private String name;
//    private int age;
//
//    public Person() {
//        this.name = "";
//        this.age = 0;
//    }
//
//    public Person(String name, int age) {
//        this.name = name;
//        this.age = age;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
//}
