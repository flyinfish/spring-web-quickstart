package org.acme.spring.web;

import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;

import org.acme.spring.web.entity.Greeting;
import org.acme.spring.web.entity.HelloParam;
import org.acme.spring.web.entity.HelloParamCheatedWithQueryParam;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.smallrye.common.constraint.NotNull;

@RestController
@RequestMapping("")
public class GreetingController {

    @GetMapping(path = "/greeting", produces = MediaType.APPLICATION_JSON_VALUE)
    public Greeting hello(@RequestParam("name") Optional<String> name) {
        return new Greeting("hello " + name.orElse("?????"));
    }

    @GetMapping(path = "/echo-param", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParam helloWithParam(HelloParam helloParam) {
        return helloParam;
    }

    @GetMapping(path = "/echo-param-at-parameter-object", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParam helloWithParamAnnotatedWithParamObject(@ParameterObject HelloParam helloParam) {
        return helloParam;
    }

    @GetMapping(path = "/echo-param-cheated-with-query-param", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParamCheatedWithQueryParam helloWithCheatedParam(HelloParamCheatedWithQueryParam helloParam) {
        return helloParam;
    }

    @GetMapping(path = "/echo-param-at-parameter-object-cheated-with-query-param", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParamCheatedWithQueryParam helloWithCheatedParamAnnotatedWithParamObject(@ParameterObject HelloParamCheatedWithQueryParam helloParam) {
        return helloParam;
    }

    @GetMapping(path = "/echo-param-cheated-with-query-param-with-validations", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParamCheatedWithQueryParam helloWithCheatedParamValidated(@BeanParam @Valid HelloParamCheatedWithQueryParam helloParam) {
        return helloParam;
    }

    @PostMapping(path = "/echo-param-cheated-with-query-param-with-validations", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParamCheatedWithQueryParam helloWithCheatedParamValidatedPost(@BeanParam @Valid HelloParamCheatedWithQueryParam helloParam,
            String body) {
        return helloParam;
    }

    @GetMapping(path = "/echo-param-at-parameter-object-cheated-with-query-param-with-validations", produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloParamCheatedWithQueryParam helloWithCheatedParamAnnotatedWithParamObjectValidated(@BeanParam @Valid @ParameterObject HelloParamCheatedWithQueryParam helloParam) {
        return helloParam;
    }
}