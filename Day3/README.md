# Day 3

## Info - Images in our Lab environment
<pre>
image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.27
image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29
image-registry.openshift-image-registry.svc:5000/openshift/wordpress:6.8.2
image-registry.openshift-image-registry.svc:5000/openshift/mariadb:11.8.2
</pre>

## Demo - In case you are curious to see how etcd db stores your deploy,rs,po
```
oc get pods -n openshift-etcd
oc project openshift-etcd
oc exec -it etcd-master03.ocp4.palmeto.org -- /bin/sh

# Equivalent to oc get pods -n jegan
etcdctl get "/kubernetes.io/pods/jegan" --prefix=True

# Equivalent to oc get deployments -n jegan
etcdctl get "/kubernetes.io/deployments/jegan" --prefix=True
```

## Lab - Testing your application by using Pod port-forward ( Not used in production )
```
oc project jegan
oc create deployment nginx --image=image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29 --replicas=3
oc get pods
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/cc049316-a924-46b4-9066-860f819062d1" />

## Lab - Getting inside a pod shell
```
oc project jegan
oc get pods
oc exec -it nginx-7667c5bd7f-67vqm -- /bin/bash
cat index.html
exit
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/b81a8fae-5ade-49fb-8c8b-1b110b87f874" />


## Lab - Let's create an internal service for our nginx deployment
```
oc project jegan
oc get deploy
oc expose deploy/nginx --type=ClusterIP --port=8080

oc get services
oc get service
oc get svc

oc describe svc/nginx
oc get pods -o wide
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/7f895510-bddf-488d-9915-ca4721e11d03" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/e3b4ae06-1ee9-480d-bd09-b3c274bdd7a5" />

<pre>
- In the above screenshot
  - nginx is the name of the service
  - port is the service port
  - targetPort is the nginx container port at which the webserver is listening
  - 172.30.188.99 is the IP address of the service,this is fake/virtual IP ( accesible only within the openshift cluster )
  - 10.131.1.155:8080,10.128.2.57:8080,10.129.2.54:8080 is the Pod endpoints
  
</pre>
