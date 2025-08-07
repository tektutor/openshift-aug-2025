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
oc port-forward pod/<your-pod-name> 9999:8080
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


## Info - Kubernetes/Openshift Service
<pre>
- In Openshift/Kubernetes service is a way we can allow either internal only clients/softwares or external end-users 
- Openshift/Kubernetes supports 2 types of Services
  1. Internal Service
     - ClusterIP Service ( kube-proxy provides load-balancing service )
  2. External
     - NodePort Service ( kube-proxy provides load-balancing service )
     - LoadBalancer Service 
       - this is generally used in public cloud environment like AWS/Azure etc.,
       - AWS Application Load Balancer or AWS Network Load Balancer or similar LB Services offered by Azure does the load balancing
       - kube-proxy is not involved in load-balanced
       - i.e if you prefer an external load balancer then we go for this type of service
  
</pre>

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
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f37270cd-4819-4c39-83f4-ff5ad796c2f1" />

<pre>
- In the above screenshot
  - nginx is the name of the service
  - port is the service port
  - targetPort is the nginx container port at which the webserver is listening
  - 172.30.188.99 is the IP address of the service,this is fake/virtual IP ( accesible only within the openshift cluster )
  - 10.131.1.155:8080,10.128.2.57:8080,10.129.2.54:8080 is the Pod endpoints
  - the endpoint controller keeps looking for new services, service update, scale up/down events, based on the selector label in the service, it identifies the pod IP addresses:<port-we-mentioned-in the command>
  - this endpoints is connected with the service
</pre>

## Demo  - Autogenerating the declarative manifest scripts
```
cd ~
mkdir practice
cd practice
oc project jegan
oc create deployment nginx \
   --image=image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29 \
   --replicas=3 \
   -o yaml \
   --dry-run=client 

oc create deployment nginx \
   --image=image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29 \
   --replicas=3 \
   -o yaml \
   --dry-run=client \
   > nginx-deploy.yml
```

## Lab - Declaratively creating nginx deployment, clusterip service and route

In case you need to delete existing deployments in your project
```
oc project jegan
oc delete deploy/nginx
```

Now you may proceed with nginx deployment
```
cd ~/openshift-aug-2025
git pull
cd Day3/declarative-manifest-scripts/nginx
cat nginx-deploy.yml

# This assumes the nginx deploy is not there already in the cluster, so it will create the nginx deploy in your project namespace
oc create -f nginx-deploy.yml --save-config

oc get deploy,rs,po
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/bffdc558-eee7-4616-8c30-ed939142e138" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/8ae649cf-5d15-4909-9fcb-f8d5121fb635" />

Now let's create declarative manifest yaml file to create a clusterip service 
```
oc get deploy
oc expose deploy/nginx --type=ClusterIP --port=8080 -o yaml --dry-run=client
oc expose deploy/nginx --type=ClusterIP --port=8080 -o yaml --dry-run=client > nginx-clusterip-svc.yml
cat nginx-clusterip-svc.yml

oc create -f nginx-clusterip-svc.yml --save-config
oc get svc
oc describe svc/nginx
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/35b92bf3-6b43-4f44-be9a-80d5e5632688" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/27eafb0e-0914-4c40-991c-3fc10fd68de8" />


Let's create an external route to access this application using public url
```
oc get svc
oc expose svc/nginx -o yaml --dry-run=client
oc expose svc/nginx -o yaml --dry-run=client > nginx-route.yml
oc create -f nginx-route.yml --save-config
oc get routes
curl http://nginx-jegan.apps.ocp4.palmeto.org 
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/0f4a9c69-6226-47b9-bf33-72b5e09c912a" />


## Lab - Creating a NodePort external service in declarative style
You need to delete the existing clusterip service we created for nginx deployment.

Kubernetes and Openshift has reserved ports in the range 30000 to 32767 for the purpose of NodePort services, when you don't mention the nodeport explicitly while creating the nodeport services, openshift will automatically pick one non-conflicting port in the range 30000-32767 which is available on the nodes of the openshift cluster and assign that to your service.
```
oc project jegan
cd ~/openshift-aug-2025
git pull
cd Day3/declarative-manifest-scripts/nginx
oc delete -f nginx-clusterip-svc.yml
```

Let's create the nodeport service in declarative style
```
oc expose deploy/nginx --type=NodePort --port=8080 -o yaml --dry-run=client > nginx-nodeport-svc.yml
oc create -f nginx-nodeport-svc.yml
oc get svc
oc describe svc/nginx
```

Accessing the NodePort service
```
curl http://<nodeip>:<node-port>
curl http://<node-hostname>:<node-port>

curl http://master01.ocp4.palmeto.org:31765
curl http://master02.ocp4.palmeto.org:31765
curl http://master03.ocp4.palmeto.org:31765
curl http://worker01.ocp4.palmeto.org:31765
curl http://worker02.ocp4.palmeto.org:31765
curl http://worker03.ocp4.palmeto.org:31765
```

## Lab - Creating a LoadBalancer external service in declarative style
You need to delete the existing nodeport service we created for nginx deployment
```
oc project jegan
cd ~/openshift-aug-2025
git pull
cd Day3/declarative-manifest-scripts/nginx
oc delete -f nginx-nodeport-svc.yml
```

Let's create the loadbalancer service in declarative style
```
oc expose deploy/nginx --type=LoadBalancer --port=8080 -o yaml --dry-run=client > nginx-lb-svc.yml
oc create -f nginx-lb-svc.yml
oc get svc
oc describe svc/nginx
```

Accessing the LoadBalancer service
```
curl http://<lb-service-external-ip-adress>:<service-port>
curl http://192.168.100.50:8080
```

## Info - Persistent Volume(PV)
<pre>
- Whenever we had to store our application data, logs in a external storage, we need Persistent Volume
- Using Pod container storage isn't recommended 
- Pod life cycle is managed by Kubernetes/Openshift
- we don't have control over the lifetime of Pods
- anytime a Pod could be deleted by Kubernetes/Openshift, it could be replaced , 
  hence using Pod container storage isn't recommended, otherwise we will lose data
- In Kubernetes/Openshift, the cluster administrator creates multiple Persistent Volumes manually as per
  different teams requirement/request
  - PV will describe
    - size of the disk in Mi, Gi
    - type of disk
    - storageclass (optional - nfs, aws s3, aws ebs, etc )
    - accessMode 
     - ReadWriteOnce
       - All Pods from a single node can access Read and Write
     - ReadWriteMany
       - All Pods from a multiple nodes can access Read and Write
</pre>

## Info - Persistent Volume Claim(PVC)
<pre>
- any Kuberentes/Openshift Pod that requires external storage, they will have describe their requirement in terms of PVC
- PVC will describe
  - size of the disk
  - type of disk
  - storageclass ( optional )
  - accessMode 
    - ReadWriteOnce
      - All Pods from a single node can access Read and Write
    - ReadWriteMany
      - All Pods from a multiple nodes can access Read and Write 
- PV can be provisioned manually by the Cluster Administrators or it can be automated by creating a StorageClass
- StorageClass can be created with your AWS account, Azure account, locally with NFS server, etc.,
- Whenever a PVC is created, in case the PVC has mentioned one of the supported StorageClass then the PV will automatically 
  provisioned dynamically and then it lets your application claim and use that PV
</pre>

## Info - Storage Controller
<pre>
- Basically , when applications request for storage by wayof PVC, storage controller is going to search the openshift cluster
  looking for a matching Persistent Volume, if it finds an exact match, then it lets your application claim and use that
- In case, there is no Persistent Volume matching the PVC then your Pod will be kept in Pending state until such a PV is created
</pre>

## Info - StorageClass
<pre>
- is a way we can provision the Persistent Volume dynamically
- you can storage from NFS, AWS S3, AWS EBS, etc.,
</pre>

## Lab - Deploying a multipod wordpress application that connects to mariadb databse with external storage (PV & PVC)

Before you proceed, you need to edit the below files and replace 'jegan' with your name. Also update the NFS Server IP address
as per your server IP 
<pre>
mysql-pv.yml
mysql-pvc.yml
myslq-deploy.yml

wordpress-pv.yml
wordpress-pvc.yml
wordpress-deploy.yml
</pre>
```
cd ~/openshift-aug-2025
git pull
cd Day3/declarative-manifest-scripts/wordpress
./deploy.sh
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/bf5c81a3-0ef4-4905-84ad-6e9b96621d72" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f4548357-8ccf-495d-970e-febdba1c46c4" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f8bff4c7-50bc-41d7-b6c4-d91131cb1d72" />

## Lab - Deploy multi-pod wordpress and mariadb pulling configurations from configmap while retreiving credentials from secrets
```
cd ~/openshift-aug-2025
git pull
cd Day3/declarative-manifest-scripts/wordpress-with-configmaps-and-secrets
./deploy.sh
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/bf5c81a3-0ef4-4905-84ad-6e9b96621d72" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f4548357-8ccf-495d-970e-febdba1c46c4" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f8bff4c7-50bc-41d7-b6c4-d91131cb1d72" />

## Info - HELM Overview
<pre>
- HELM is a package manager for Kuberentes and Openshift
- Using Helm package manager we can install/uninstall/upgrade application with Kubernetes/Openshift
- Helm has opensource repository website from where one could download available helm charts and install onto our cluster
- Helm can also be used to package our application manifest scripts and bundle as Helm Charts ( tar ball - compressed file with a specific directory structure )
</pre>

## Demo - Installing Helm
```
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

## Lab - Creating a custom helm chart to package our wordpress, mysql multipod application
```
cd ~/openshift-aug-2025
git pull
cd Day3/helm-charts
tree scripts
helm create wordpress
cp values.yaml wordpress
tree wordpress
cd wordpress 
# edit the values.yaml and customize your changes ( nfs_server_ip, your_name, nfs_wordpress_path, nfs_mysql_path, etc., ) 
cd templates
rm *
rm -rf test
cd ../../
cp scripts/* wordpress/templates
tree wordpress
helm package wordpress
ls
helm install wordpress wordpress-0.1.0.tgz
helm list
oc get pods -w
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/1f56d6df-4b3e-4eca-9f14-ead920edac9b" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/2f53029d-9723-40e3-b7ef-19c3916c802a" />
