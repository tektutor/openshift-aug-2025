# Day 2

## Info - Container Orchestration Platform Overview
<pre>
- High Availability is one of the main motivation for using Container Orchestration Platform
- It supports in-built monitoring tools 
- It supports scaling up your containerized application workloads when the user-traffic increases
- It supports scaling down your containerized application workloads when the user-traffic decreases
- scale up/down can be configured to happen automatically or can be managed manually
- upgrading your application from one version to the other without any downtime can be done with rolling update feature
- rollback can be done when the newly rolled out application is found to be unstable
- RBAC - Role based access control is supported, hence only authorized users can access your applications or deploy applications
- some orchestration platform supports only CLI, while others also support web console(Dashboard - GUI)
- allows you to expose your applications to internal use only or if required can be allowed for external access via services
- centralized logging is supported
  - splunk ( can be integrated on demand )
  - ELK/EFK ( Elastic Search, LogStash and Kibana or Elastic Search, Fluentd & Kibana )
- supports alerts & notification
- certain container orchestration platform even support scaling up/down server in your cluster ( orchestration platform )
- some of the container orchestration platforms support CI/CD
- all the application that must be deployed into Container Orchestration Platforms must be containerized
- both monolithic and microservice applications are supported
- examples
  - Docker SWARM
  - Kubernetes
  - Red Hat Openshift
</pre>

## Info - Docker SWARM Overview
<pre>
- it is Docker's native Container Orchestration Platform
- supports only Docker containerized application workloads
- it is opensource
- very light weight, hence can be setup in laptops with normal Hardware configurations
- highly recommended for learning purpose, Dev/QA environment for light-weight setup and testing purposes
- not production grade, hence generally no one uses them in production
</pre>

## Info - Kubernetes Overview
<pre>
- this is an opensource Container Orchestration Platform Google
- is developed in Go langanguage
- but one can deploy C/C++, Java, Dot Net, Python, Perl, pretty much any application into Kuberentes
- it is highly robust and production grade
- many companies use them in production
- it supports only command-line interface, no GUI/Dashboard
- if you like like, you could try using Rancher, which provide Webconsole over Kubernetes
- Supports services
  - to expose your application for internal only consumption
  - to expose your applicaiton for external access
- Supports Custom Resource Definitions a.k.a CRD to extend Kubernetes to support new type of Resources
- it supports extending Kubernetes functionality by developing your own Kubernetes Operators, or your could download and install third-party operators within Kubernetes
- no support from Google
- there are managed Kubernetes from AWS, Azure, Google cloud
  - EKS - Elastic Kubernetes Service from AWS ( you get support for this from Amazon )
  - AKS - Azure Kubernetes Service from Azure ( you get support for this from Microsoft )
  - GKE - you get support this from Google
- installation is time consuming, not easy
- application can only be deploy if you have a container image
</pre>

## Info - Kubernetes High-level Architecture
![Kubernetes](KubernetesArchitecture2.png)

## Info - Red Hat Openshift Overview
<pre>
- it is Red Hat's distributio of Kubernetes
- it is an enterprise product, requires license to use it
- developed on top of Open source Google Kubernetes, but this is paid software
- will get world-wide support from Red Hat
- installation is very difficult and there are numerous ways to install Openshift, much difficult than Kubernetes 
  installation
- there are two types of installation
  1. Installer Provisioned Installation (IPI)
     - automated installation
     - very easy to install in any public cloud like AWS, Azure, Google Cloud
  2. User Provisioned Installation (UPI)
     - is tough one
     - installing in bare-metal local server are very difficult and offers same level security and robustness like the ones supported by managed services
- you will also managed Red Hat Openshift cluster from public cloud
  - ROSA ( managed Openshift Cluster from AWS )
  - ARO ( managed Openshift Cluster from Azure )
- supports User Management
- comes with pre-integrated monitoring tools like Prometheus & Graphana Visual Dashboards
- comes with Internal Openshift Image Registry
- comes with many additional features on top of Kubernetes
  - Build, BuildConfig, Route, DeploymentConfig, etc.,
  - S2I ( application can be deployed from source code )
- Red Hat supports Universal Bases Images (UBI) for most popular languages
</pre>

## Info - Red Hat Openshift High-level Architecture
![Openshift](openshiftArchitecture.png)

## Lab - Checking if openshift tools are installed
```
oc version
kubectl version

oc get nodes
oc get nodes -o wide

kubectl get nodes
kubectl get nodes -o wide
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/b227e331-3a99-4f43-a983-b8279a32fb7b" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/aeb81b4f-b091-4505-9c48-1e7f8c258c83" />

## Lab - Find more detailsmaster01.ocp4.palmeto.org one of the master nodes
```
oc get nodes
oc describe node master01.ocp4.palmeto.org
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/cc1161b3-cd70-4aaf-aab8-a08cecf8bc5f" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/905f90e8-a7c9-4072-9793-5b6190d93e54" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/72bbc778-a25e-49ad-872e-19bf2bb84f1d" />



