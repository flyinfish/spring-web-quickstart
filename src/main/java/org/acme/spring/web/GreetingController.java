package org.acme.spring.web;

import java.util.Optional;

import org.acme.spring.web.entity.Greeting;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/greeting")
public class GreetingController {

    @Tag(name = "basic")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Greeting hello(@RequestParam("name") Optional<String> name) {
        return new Greeting("hello " + name.orElse("?????"));
    }
}