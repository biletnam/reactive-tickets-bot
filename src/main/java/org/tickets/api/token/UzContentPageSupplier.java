package org.tickets.api.token;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.Materializer;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;


public class UzContentPageSupplier implements Supplier<String> {

    private final Materializer materializer;
    private final ActorSystem actorSystem;

    public UzContentPageSupplier(Materializer materializer, ActorSystem actorSystem) {
        this.materializer = materializer;
        this.actorSystem = actorSystem;
    }

    @Override
    public String get() {

        CompletionStage<HttpResponse> request = Http
                .get(actorSystem)
                .singleRequest(null, materializer);

        return null;
    }
}
