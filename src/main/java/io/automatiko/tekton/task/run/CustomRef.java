package io.automatiko.tekton.task.run;

public class CustomRef {

    private String apiVersion;

    private String kind;

    private String name;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Ref [apiVersion=" + apiVersion + ", kind=" + kind + ", name=" + name + "]";
    }
}
