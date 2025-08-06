# Day 3

## Info - Images in our Lab environment
<pre>
image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.27
image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29
image-registry.openshift-image-registry.svc:5000/openshift/wordpress:6.8.2
image-registry.openshift-image-registry.svc:5000/openshift/mariadb:11.8.2
</pre>

## Lab - Testing your application by using Pod port-forward ( Not used in production )
```
oc project jegan
oc create deployment nginx --image=image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29 --replicas=3
oc get pods
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/cc049316-a924-46b4-9066-860f819062d1" />
