package fr.softeam.opus.userskillmgmt.verticle;

import fr.softeam.opus.userskillmgmt.configuration.handler.RequestLoggerHandler;
import fr.softeam.opus.userskillmgmt.services.HelloService;
import fr.softeam.opus.userskillmgmt.services.VersionService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UserSkillMgmtVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSkillMgmtVerticle.class);

    @Inject
    private HelloService helloService;

    @Inject
    private VersionService versionService;

    private HttpServer server;
    private ServiceBinder serviceBinder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Future<Void> future) {
        LOGGER.info("Starting User Skill MGMT verticle.");

        // register services
        startHelloService();
        startVersionService();

        //start server
        startHttpServer().setHandler(future.completer());
    }

    @Override
    public void stop(){
        this.server.close();
        consumer.unregister();
    }

    private void startHelloService() {
        serviceBinder = new ServiceBinder(vertx);

        consumer = serviceBinder
                .setAddress("hello_service.usm")
                .register(HelloService.class, helloService);

    }

    private void startVersionService() {
        serviceBinder = new ServiceBinder(vertx);

        consumer = serviceBinder
                .setAddress("version_service.usm")
                .register(VersionService.class, versionService);

    }

    private Future<Void> startHttpServer() {
        Future<Void> future = Future.future();
        OpenAPI3RouterFactory.create(this.vertx, "/openapi.yaml", openAPI3RouterFactoryAsyncResult -> {
            if (openAPI3RouterFactoryAsyncResult.succeeded()) {
                OpenAPI3RouterFactory routerFactory = openAPI3RouterFactoryAsyncResult.result();

                routerFactory.mountServicesFromExtensions();

                Router router = routerFactory.getRouter();
                router.route().handler(RequestLoggerHandler.create());

                server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
                server.requestHandler(router).listen(ar -> {
                    if (ar.succeeded()) {
                        future.complete();
                    }
                    else {
                        future.fail(ar.cause());
                    }
                });
            } else {
                // Something went wrong during router factory initialization
                future.fail(openAPI3RouterFactoryAsyncResult.cause());
            }
        });
        return future;
    }
}


