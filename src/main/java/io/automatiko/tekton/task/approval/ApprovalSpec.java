package io.automatiko.tekton.task.approval;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApprovalSpec implements KubernetesResource {

    private static final long serialVersionUID = 1L;

    public enum Strategy {
        SINGLE,
        MULTI,
        FOUR_EYES
    }

    private String description;
    private List<String> approvers;
    private List<String> groups;
    private String pipeline;
    private Strategy strategy;

    private List<Map<String, String>> data;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getApprovers() {
        return approvers;
    }

    public void setApprovers(List<String> recipient) {
        this.approvers = recipient;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    public Map<String, String> flatData() {

        return data.stream().flatMap(item -> item.entrySet().stream())
                .collect(Collectors.toMap(item -> item.getKey(), item -> item.getValue()));
    }

    @Override
    public String toString() {
        return "ApprovalSpec [description=" + description + ", approvers=" + approvers + ", groups=" + groups + ", pipeline="
                + pipeline + ", strategy=" + strategy + "]";
    }

}
