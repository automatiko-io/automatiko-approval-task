# Automatiko Approval Tasks for Tekton

Automatiko Approval Tasks is an implementation for [Tekton](https://tekton.dev) that allows to pause the execution 
of the pipeline run and wait for approval by human actors. It is an implementation based on [custom tasks](https://tekton.dev/docs/pipelines/runs/) of Tekton that is currently in *v1alpha1* stage which is considered not stable.

## Features

See [Roadmap](Roadmap.md) for already defined features to be added. Feel free to ask for additional ones either by creating an issue or adding to the roadmap.


Approval Task allows to use approvals as part of you pipeline definition. Let's look quickly at very simple example pipeline

````

apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: testpipeline
spec:
  tasks:
    - name: approval
      taskRef:
        apiVersion: tekton.automatiko.io/v1alpha1
        kind: ApprovalTask
        name: approvaltask
      params:
        - name: pipeline
          value: "$(context.pipelineRun.name)"
        - name: description
          value: "Sample approval from pipeline $(context.pipeline.name)"
        - name: approvers
          value:
            - "john"
    - name: approved
      when:
        - input: $(tasks.approval.results.decision)
          operator: in
          values: [ "true" ]
      taskRef:
        name: print-decision
      runAfter:
        - approval
      params:
        - name: decision
          value: "APPROVED"
        - name: comment
          value: $(tasks.approval.results.comment)
    - name: rejected
      when:
        - input: $(tasks.approval.results.decision)
          operator: in
          values: [ "false" ]
      taskRef:
        name: print-decision
      runAfter:
        - approval
      params:
        - name: decision
          value: "REJECTED"
        - name: comment
          value: $(tasks.approval.results.comment)          
````

where the first task of the pipeline is to wait for approval from a single approver - *john* and then switch either to go to approved or rejected task that simply prints out the decision and comment.

Approval task is identified with following

- **apiVersion: tekton.automatiko.io/v1alpha1**
- **kind: ApprovalTask**
- **name: approvaltask**

### Parameters

It accepts several parameters

- **pipeline** - name of the pipeline this approval is associated with
- **description** - human focused description of the operation to be done
- **approvers** - list of approvers (usernames or email addresses) that task should be assigned to
- **strategy** - strategy of the approval task - currently supported **SINGLE** and **MULTI** - if not set defaults to SINGLE

### Strategies

#### SINGLE

Single strategy means that there will be just one task created, regardless how many approvers where set. Each approver is equally eligible to approve or reject the task ad once one of them decides the pipeline will continue.

#### MULTI

Multi strategy means that for every approver set there will be dedicated task assigned and each and every one of them must make the decision to continue pipeline run.

### Results

Upon completion, approval task will set following results that can be used by further tasks of the pipeline

- **decision** either `true` or `false` that corresponds to approved and rejected 
- **comment** optional comment given by approvers, in case of multi strategy used, each comment will be separated by `|` symbol

An example how to use it can be as follows

````
- name: approved
  when:
    - input: $(tasks.approval.results.decision)
      operator: in
      values: [ "true" ]
  taskRef:
    name: print-decision
  runAfter:
    - approval
  params:
    - name: decision
      value: "APPROVED"
    - name: comment
      value: $(tasks.approval.results.comment)
````

### Notifications

Approval tasks can be configured with email notification support that will send emails to approvers upon task creation.
To make use of it, email server must be configured as part of the deployment and approvers must be given as email addresses.

````
  tasks:
    - name: approval
      taskRef:
        apiVersion: tekton.automatiko.io/v1alpha1
        kind: ApprovalTask
        name: approvaltask
      params:
        - name: pipeline
          value: "$(context.pipelineRun.name)"
        - name: description
          value: "Sample approval from pipeline $(context.pipeline.name)"
        - name: approvers
          value:
            - "john@email.com"
            - "mary@email.com"            
````

### Tekton Dashboard integration

Approval tasks come with integration with Tekton Dashboard. It shows up in the **Extensions** sections and allow to view approval tasks as long as their pipeline run exists within the cluster.

<img src="img/1.png" width="800px"/>

As part of the detail view of the approval task you can see directly information about decision on given task and how many approvers have already made decisions.

<img src="img/2.png" width="800px"/>

### Approval forms

Approval tasks come with basic UI capabilities to help approvers provide their decision (approve or reject). When email notifications are used, then each approver will get an email with dedicated link to the form.

<img src="img/3.png" width="800px"/>

On the form there is option to add optional comment that will be then available in the further tasks of the pipeline as part of the decision results.

<img src="img/4.png" width="800px"/>

Approval tasks can also be found via direct access form that can be found on the root URL of the service deployed as part of this project.

<img src="img/5.png" width="800px"/>

In the form you need to provide

- pipeline run name - name of the pipeline run you want to find tasks for
- approver - approver (user name or email or whatever else was used in pipeline definition) that tasks should be found for

This will provide a list of tasks available with links to the form to provide decision.

### Management view (for admins only)

Approval tasks comes with management view that is giving an internal insight into what is behind the tasks. It is process management view as approval tasks are based on workflows (built with [Automatiko project](https://github.com/automatiko-io/automatiko-engine)). You can see there details on each  workflow instance that is used behind the scenes to provide this feature to tekton pipelines.

<img src="img/6.png" width="800px"/>

## Installation

This section will guide you through all the steps to make approval tasks run in tekton.

### Install Tekton and Tekton Dashboard

Following command will install Tekton and Tekton Dashboard into your Kubernetes cluster

````
kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml

kubectl apply --filename https://github.com/tektoncd/dashboard/releases/latest/download/tekton-dashboard-release.yaml
````

### Configure Tekton

To be able to use Tekton with Approval Tasks you need to enable use of custom tasks.

````
kubectl edit cm feature-flags -n tekton-pipelines
````

In there find the line for `enable-custom-tasks` and check its value to `true`


### Install Approval Tasks CRD

NOTE: All scripts for Approval Tasks can be found in `k8s` directory of this repository

First step is to create CRD of Approval Tasks

````
kubectl apply -f k8s/approvaltasks.tekton.automatiko.io-v1.yml
````

### Install Approval Task Tekton Dashboard extension


To be able to see Approval Tasks in Tekton Dashbord there is a need to create extension and cluster role and cluster role binding for it to allow Tekton Dashboard read and watch approval tasks objects.

````
kubectl apply -f k8s/approvaltasks-dashboard-ext.yaml
kubectl apply -f k8s/approvaltask-dashboard-cr.yaml
kubectl apply -f k8s/approvaltasks-dashboard-crb.yaml
````

### Install approval tasks

Lastly, install the approval tasks into your cluster

NOTE: It comes with default values for service URL and namespaces to watch. Please look into them before deployment, there are represented as environment variables of the deployment and have following values


````
- name: QUARKUS_OPERATOR_SDK_NAMESPACES
  value: default
- name: QUARKUS_AUTOMATIKO_SERVICE_URL
  value: http://localhost:9000
````

Change them accordingly to your environment needs.

````
kubectl apply -f k8s/kubernetes-basic.yml
````

If you want to use approval tasks with email notifications configure email server and use 

````
kubectl apply -f k8s/kubernetes-email.yml
````

#### Configure email server

To enable email notification, approval tasks deployment must be equipped with additional environment variables. A template for it can be found in `k8s` directory.

````
- name: QUARKUS_MAILER_FROM
  value: youruser@gmail.com
- name: QUARKUS_MAILER_HOST
  value: smtp.gmail.com              
- name: QUARKUS_MAILER_PORT
  value: "587"
- name: QUARKUS_MAILER_USERNAME
  value: youruser@gmail.com
- name: QUARKUS_MAILER_PASSWORD
  value: password        
````

Above properties must be set based on your email server before deployment.

#### Ingress

Default deployment scripts create on service but not ingress as it really depends on your target kubernetes cluser. So this needs to be configured separately and the URL used on the ingress must be used for `QUARKUS_AUTOMATIKO_SERVICE_URL` in deployment.

for minikube you can use port forward for both Tekton Dashboard and Approval Tasks

```
kubectl --namespace tekton-pipelines port-forward svc/tekton-dashboard 9097:9097

kubectl port-forward svc/automatiko-approval-task 9000:80

````

# Use it

Once a instance is complete you can deploy the task and pipeline to get the first approval task from Tekton pipeline.

````
kubectl apply -f k8s/test/print.yaml
kubectl apply -f k8s/test/pipeline-single.yaml
````

Then head to Tekton Dashboard where you should see something like this

<img src="img/7.png" width="800px"/>

and just run an instance of the pipeline. Next you can go to Approval Tasks in the Extensions and should see one instance there too. If you to into details you will see labels that indicate the state of it

<img src="img/8.png" width="800px"/>

and when going to YAML view you will see an `approvalUrl` in the status section. Copy that url and open in new window, it will take you to either search for (when using MULTI strategy) or directly to the form (when using single strategy).

<img src="img/9.png" width="800px"/>

If you use single strategy and assigned approvers to it, you mign need to append `?user=YOUR_USER_NAME` to the url.

<img src="img/10.png" width="800px"/>

Once you made decision (approve or reject) the pipeline run will complete.

<img src="img/11.png" width="800px"/>

and the approval task instance will also be updated (look at labels)

<img src="img/12.png" width="800px"/>

If you want o clean it up, just delete pipeline run and all other resources will be automatically cleaned. This is actually a good practice to have some sort of cleanup of complete pipeline runs to ensure the best performance.
