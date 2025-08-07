# Day 4

## Info - Container Images supported by our Lab
<pre>
image-registry.openshift-image-registry.svc:5000/openshift/spring-ms:1.0
image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.27
image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29
image-registry.openshift-image-registry.svc:5000/openshift/wordpress:6.8.2
image-registry.openshift-image-registry.svc:5000/openshift/mariadb:11.8.2
</pre>  


## Info - Ingress Overview
<pre>
- is not a service
- is a forward/routing rules
- for this is work, your openshift cluster must have an Ingress Controller installed in it
- some commonly used Ingress Controller are
  1. Nginx Ingress Controller ( open source )
  2. HAProxy Ingress Controller ( open source )
  3. F5 ( Enterprise variant of Nginx - Paid software )
- Let's we have HDFC Bank, and its home page is https://www.hdfcbank.com
- Ingress host url looks like a home page of a web site
- Assume this banking application has many microservices 
  - Customer Microservice
  - Statement Microservice
  - Loans Microservice
  - Cheque Microservice
  - Account Microservice
  - Authentication Microservice
- When you are navigating to www.hdfcbank.com/login, then you wish the control forwarded to Authentication Microservice ( clusterip or nodeport or loadbalancer service )
- When you are navigating to www.hdfcbank.com/loans, then you wish the control forwarded to Loan Microservice ( clusterip or nodeport or loadbalancer service )
- Ingress represents a group of hetrogeneous Kubernetes/Openshift services ( NodePort, ClusterIP, etc)
</pre>

## Demo - Integrating LDAP with Red Hat Openshift ( Bonus Topic )

Install OpenLDAP in Ubuntu
```
sudo apt update
sudo apt install slapd ldap-utils
```

Configure OpenLDAP
```
# Reconfigure slapd for proper setup
sudo dpkg-reconfigure slapd
```

Start OpenLDAP Server
```
sudo systemctl status slapd
sudo systemctl enable slapd
sudo systemctl start slapd
sudo systemctl status slapd
```

Create LDAP structure and users (LDIF file)
```
dn: ou=people,dc=palmeto,dc=org
objectClass: organizationalUnit
ou: people

dn: ou=groups,dc=palmeto,dc=org
objectClass: organizationalUnit
ou: groups
```

Create a user
```
dn: uid=jegan,ou=people,dc=palmeto,dc=org
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: jegan
sn: Swaminathan
givenName: Jeganathan
cn: Jeganathan Swaminathan
displayName: Jeganathan Swaminathan
uidNumber: 1001
gidNumber: 1001
userPassword: {SSHA} root@123
gecos: Jeganathan Swaminathan
loginShell: /bin/bash
homeDirectory: /home/jegan
```

Add the user
```
ldapadd -x -D "cn=admin,dc=palmeto,dc=org" -W -f /tmp/base.ldif
ldapadd -x -D "cn=admin,dc=palmeto,dc=org" -W -f /tmp/user.ldif
```

Integrate OpenLDAP with OpenShift v4.19 (ldap-idp.yaml)
```
apiVersion: config.openshift.io/v1
kind: OAuth
metadata:
  name: cluster
spec:
  identityProviders:
  - name: ldap
    mappingMethod: claim
    type: LDAP
    ldap:
      attributes:
        id:
        - dn
        preferredUsername:
        - uid
        name:
        - cn
        email:
        - mail
      bindDN: "cn=admin,dc=palmeto,dc=org"
      bindPassword:
        name: ldap-secret
      insecure: true
      url: "ldap://192.168.10.200:389/ou=people,dc=palmeto,dc=org?uid"
```

Create LDAP bind password secret
```
oc create secret generic ldap-secret \
  --from-literal=bindPassword=root@123 \
  -n openshift-config
```

Create LDAP server certificate
```
# Get LDAP server certificate
openssl s_client -connect your-ldap-server:636 -showcerts < /dev/null 2>/dev/null | openssl x509 -outform PEM > ldap-ca.crt

# Create configmap
oc create configmap ldap-ca \
  --from-file=ca.crt=ldap-ca.crt \
  -n openshift-config
```

Create the LDAP Identify Provider Configuration
```
oc apply -f ldap-idp.yaml
```

Create ClusterRoleBinding for LDAP users
```
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: ldap-cluster-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- apiGroup: rbac.authorization.k8s.io
  kind: User
  name: jegan
```
```
oc apply -f /tmp/ldap-admin-binding.yml
```

Verify the integration
```
#Update password
ldappasswd -x -H ldap://192.168.10.200:389 -D "cn=admin,dc=palmeto,dc=org" -W -S "uid=jegan,ou=people,dc=palmeto,dc=org"

# Test authentication after password update
ldapwhoami -x -H ldap://192.168.10.200:389 -D "uid=jegan,ou=people,dc=palmeto,dc=org" -W
# Enter password: root@123

#Alternate approach
slappasswd -s "root@123"


# Check OAuth configuration
oc get oauth cluster -o yaml | grep -A 20 "ldap:"

# Check authentication operators
oc get pods -n openshift-authentication-operator

# If still showing ldaps://, reapply the configuration
oc apply -f /tmp/ldap-non-ssl.yaml

# Wait for OAuth configuration to propagate (2-3 minutes)
sleep 180

# Try OpenShift login
oc login --username=jegan --password='root@123' --insecure-skip-tls-verify

# In another terminal, monitor authentication attempts
oc logs -n openshift-authentication deployment/oauth-openshift -f | grep -E "(jegan|ldap|bind|authentication|error)"
```

## Lab - Ingress

Let's delete our existing project
```
oc delete project jegan
```

Let's create a new project
```
oc new-project jegan
```

Let's deploy two applications
```
oc create deploy nginx --image=image-registry.openshift-image-registry.svc:5000/openshift/nginx:1.29 --replicas=3
oc create deploy hello --image=image-registry.openshift-image-registry.svc:5000/openshift/spring-ms:1.0 --replicas=3

oc expose deploy/nginx --port=8080
oc expose deploy/hello --port=8080
```

Let's create an ingress ingress.yml file
<pre>
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tektutor
  annotations:
    haproxy.router.openshift.io/rewrite-target: /
spec:
  rules:
    - host: tektutor.apps.ocp4.palmeto.org
      http:
        paths:
        - backend:
            service:
              name: nginx
              port:
                number: 8080
          path: /nginx
          pathType: Prefix
        - backend:
            service:
              name: hello 
              port:
                number: 8080
          path: /hello
          pathType: Prefix  
</pre>

Let's create the ingress
```
oc apply -f ingress.yml
oc get ingress

http://tektutor.apps.ocp4.palmeto.org/nginx
http://tektutor.apps.ocp4.palmeto.org/hello
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/2b7658dd-8a42-44ca-91c7-3d74d1edf52a" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/5c9a4f75-9856-4fd8-855d-e113956373d2" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/c8c41f27-0ce0-4fc3-828e-5e5d634ed76f" />


## Lab - Scheduler Node Affinity

<pre>
- There are 2 types of Node Affininty
  1. Preferred
     - Scheduler will attempt to schedule the pods onto to nodes that meet the criteria mentioned
     - in case Scheduler is not able to find such nodes, it will normally deploy them into any nodes irrespective they meet the criteria or not 
  2. Required
     - SCheduler will only schedule the pods onto the nodes that meet the criteria
     - Until such nodes are found, the Pod will be kept in Pending state
</pre>

Preferred Nodeaffinity
```
cd ~/openshift-aug-2025
git pull
cd Day4/scheduler-node-affinity

# First list and check if there are any nodes that has SSD disk, assuming no nodes meets the criteria
oc get nodes -l disk=ssd

oc apply -f nginx-preferred-deploy.yml
# We understood the scheduler will go ahead and deploy pods on any node in case no nodes meets such criteria
oc get pods -o wide
# Let's delete the deployment
oc delete -f nginx-preferred-deploy.yml

#Let's label worker03 with disk=ssd label
oc label node worker03.ocp4.palmeto.org disk=ssd
oc get nodes -l disk=ssd
oc apply -f nginx-preferred-deploy.yml
# We can see schduler did respect the application scheduling preference
oc get pods -o wide
# Let's delete the deployment
oc delete -f nginx-preferred-deploy.yml

# Let's deploy nginx-required-deploy.yml
oc get nodes -l disk=ssd
oc apply -f nginx-required-deploy.yml
# We can see all 3 pods are deployed onto worker03 as worker03 is the only node that meets the criteria
oc get pods -o wide
# Let's delete the deployment
oc delete -f nginx-preferred-deploy.yml


#Now, let's remove the label from worker03 node
oc label node worker03.ocp4.palmeto.org disk-
oc get nodes -l disk=ssd
oc apply -f nginx-required-deploy.yml
# We can see 3 Pods are in Pending state as no nodes meet the criteria imposed by your deployment
oc get pods -o wide
# Let's delete the deployment
oc delete -f nginx-preferred-deploy.yml
```
