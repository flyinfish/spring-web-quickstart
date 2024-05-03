package org.acme.spring.web;

import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.acme.spring.web.entity.Greeting;
import org.acme.spring.web.entity.HelloParam;
import org.acme.spring.web.entity.HelloParamCheatedWithQueryParam;

@Path("/jakartars")
public class GreetingResource {

    @GET()
    @Path("/greeting")
    @Produces("application/json")
    public Greeting hello(@QueryParam("name") Optional<String> name) {
        return new Greeting("hello " + name.orElse("?????"));
    }

    @GET()
    @Path("/echo-param")
    @Produces("application/json")
    public HelloParam helloWithParam(HelloParam helloParam) {
        return helloParam;
    }

    @GET()
    @Path("/echo-param-with-query-param")
    @Produces("application/json")
    public HelloParamCheatedWithQueryParam helloWithQueryParam(HelloParamCheatedWithQueryParam helloParam) {
        return helloParam;
    }

    @GET()
    @Path("/echo-param-with-query-param-with-validations")
    @Produces("application/json")
    public HelloParamCheatedWithQueryParam helloWithCheatedParamValidated(@BeanParam @Valid HelloParamCheatedWithQueryParam helloParam) {
        return helloParam;
    }

}