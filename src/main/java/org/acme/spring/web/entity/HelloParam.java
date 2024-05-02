package org.acme.spring.web.entity;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class HelloParam {
    @NotNull
    private Title title;
    @NotNull
    @NotEmpty
    private String name;
    private String suffix;

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
