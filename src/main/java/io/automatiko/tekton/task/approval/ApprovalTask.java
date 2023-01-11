package io.automatiko.tekton.task.approval;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("tekton.automatiko.io")
@Version("v1beta1")
public class ApprovalTask extends CustomResource<ApprovalSpec, ApprovalStatus> implements Namespaced {

    public static final String GROUP = "tekton.automatiko.io";
    public static final String VERSION = "v1beta1";
    public static final String KIND = "ApprovalTask";
    public static final String API_VERSION = GROUP + "/" + VERSION;

    private static final long serialVersionUID = 1L;

    @Override
    protected ApprovalSpec initSpec() {
        return new ApprovalSpec();
    }

    @Override
    protected ApprovalStatus initStatus() {
        return new ApprovalStatus();
    }

}
