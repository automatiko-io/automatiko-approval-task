package io.automatiko.tekton.task.run;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("tekton.dev")
@Version("v1beta1")
public class CustomRun extends CustomResource<CustomRunSpec, CustomRunStatus> implements Namespaced {

    private static final long serialVersionUID = 1L;

    @Override
    protected CustomRunSpec initSpec() {
        return new CustomRunSpec();
    }

    @Override
    protected CustomRunStatus initStatus() {
        return new CustomRunStatus();
    }

}
