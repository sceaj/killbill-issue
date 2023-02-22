package sceaj.killbill.test.plugin.model;

import java.time.LocalDate;

public class TestData {

    private String externalId;
    private LocalDate someDate;
    private Double someValue;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public LocalDate getSomeDate() {
        return someDate;
    }

    public void setSomeDate(LocalDate someDate) {
        this.someDate = someDate;
    }

    public Double getSomeValue() {
        return someValue;
    }

    public void setSomeValue(Double someValue) {
        this.someValue = someValue;
    }
}
