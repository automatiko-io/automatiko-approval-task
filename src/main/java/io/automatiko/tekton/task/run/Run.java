package io.automatiko.tekton.task.run;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("tekton.dev")
@Version("v1alpha1")
public class Run extends CustomResource<RunSpec, RunStatus> implements Namespaced {

    private static final long serialVersionUID = 1L;

    @Override
    protected RunSpec initSpec() {
        return new RunSpec();
    }

    @Override
    protected RunStatus initStatus() {
        return new RunStatus();
    }

}
