package it.pagopa.swclient.mil.idpay.bean;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class Initiative {
    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String id;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String name;

    @NotNull
    @Pattern(regexp = "^[\u0001-\uD7FF\uE000-\uFFFD\u10000-\u10FFFF]{1,256}$")
    private String organization;

    public Initiative() {
    }

    public Initiative(@NotNull String id, @NotNull String name, @NotNull String organization) {
        this.id = id;
        this.name = name;
        this.organization = organization;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return "Initiative{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }
}
