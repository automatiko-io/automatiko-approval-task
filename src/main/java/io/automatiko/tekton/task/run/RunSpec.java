package io.automatiko.tekton.task.run;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = JsonDeserializer.None.class)
public class RunSpec implements KubernetesResource {

    private static final long serialVersionUID = -1775061327392758141L;
    private List<Map<String, Object>> params;

    private Ref ref;

    public Ref getRef() {
        return ref;
    }

    public void setRef(Ref ref) {
        this.ref = ref;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public Object findParam(String name, Object defaultValue) {

        if (params == null) {
            return defaultValue;
        }

        return params.stream().filter(param -> param.getOrDefault("name", "").equals(name)).map(param -> param.get("value"))
                .findFirst()
                .orElse(defaultValue);
    }
}
