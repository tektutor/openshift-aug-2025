# Day 4

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
# Check OAuth configuration
oc get oauth cluster -o yaml

# Check authentication operators
oc get pods -n openshift-authentication-operator

# Test login via web console or CLI
oc login --username=jegan --password=root@123 --insecure-skip-tls-verify
```
