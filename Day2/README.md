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
</pre>
