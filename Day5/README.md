# Day 5

## Info - LDAP Integration with Openshift
<pre>
- You can now login to our Openshift webconsole with your linux user name
- Password is palmeto@123
- In case you prefer using kubeadmin as usual you can continue using the same credential
</pre>  

## Flannel Network Model
<pre>
Flannel is a simple and easy way to configure a layer 3 network fabric designed for Kubernetes. 
It provides networking for container clusters by creating a virtual network that spans across all nodes.

Key Concepts
- Overlay Network
- Creates a virtual network layer on top of the existing host network
- Each node gets a subnet from a larger cluster network
- Pods can communicate across nodes using this overlay
- Network Backends
- VXLAN (Default)

Encapsulates packets in UDP
- Works across most network infrastructures
- Good performance with modern kernels

Host-Gateway
- Uses host routing tables
- Better performance but requires Layer 2 connectivity
- No packet encapsulation overhead

UDP
- Legacy backend
- User-space packet forwarding
- Lower performance but maximum compatibility
</pre>

#### Architecture
<pre>
┌─────────────────────────────────────┐    ┌─────────────────────────────────────┐
│              Node A                 │    │              Node B                 │
│          (10.0.1.100)               │    │          (10.0.1.101)               │
│                                     │    │                                     │
│ ┌─────────────┐    ┌─────────────┐  │    │ ┌─────────────┐    ┌─────────────┐  │
│ │    Pod1     │    │    Pod2     │  │    │ │    Pod3     │    │    Pod4     │  │
│ │             │    │             │  │    │ │             │    │             │  │
│ │ eth0:       │    │ eth0:       │  │    │ │ eth0:       │    │ eth0:       │  │
│ │10.244.1.10  │    │10.244.1.20  │  │    │ │10.244.2.10  │    │10.244.2.20  │  │
│ │             │    │             │  │    │ │             │    │             │  │
│ │ Route:      │    │ Route:      │  │    │ │ Route:      │    │ Route:      │  │
│ │ default via │    │ default via │  │    │ │ default via │    │ default via │  │
│ │10.244.1.1   │    │10.244.1.1   │  │    │ │10.244.2.1   │    │10.244.2.1   │  │
│ └─────────────┘    └─────────────┘  │    │ └─────────────┘    └─────────────┘  │
│        │                   │        │    │        │                   │        │
│        └───────┬───────────┘        │    │        └───────┬───────────┘        │
│                │                    │    │                │                    │
│         ┌─────────────┐             │    │         ┌─────────────┐             │
│         │   cni0      │             │    │         │   cni0      │             │
│         │ 10.244.1.1  │             │    │         │ 10.244.2.1  │             │
│         │ (bridge)    │             │    │         │ (bridge)    │             │
│         └─────────────┘             │    │         └─────────────┘             │
│                │                    │    │                │                    │
│         ┌─────────────┐             │    │         ┌─────────────┐             │
│         │  flannel.1  │             │    │         │  flannel.1  │             │
│         │ 10.244.1.0  │             │    │         │ 10.244.2.0  │             │
│         │ (vxlan)     │             │    │         │ (vxlan)     │             │
│         └─────────────┘             │    │         └─────────────┘             │
│                │                    │    │                │                    │
│         ┌─────────────┐             │    │         ┌─────────────┐             │
│         │    eth0     │             │    │         │    eth0     │             │
│         │ 10.0.1.100  │             │    │         │ 10.0.1.101  │             │
│         │ (physical)  │             │    │         │ (physical)  │             │
│         └─────────────┘             │    │         └─────────────┘             │
│                                     │    │                                     │
│ Routing Table (Node A):             │    │ Routing Table (Node B):             │
│ ┌─────────────────────────────────┐ │    │ ┌─────────────────────────────────┐ │
│ │ Destination    Gateway   Iface  │ │    │ │ Destination    Gateway   Iface  │ │
│ │ 10.244.1.0/24  0.0.0.0   cni0   │ │    │ │ 10.244.2.0/24  0.0.0.0   cni0   │ │
│ │ 10.244.2.0/24  10.244.2.0 fl.1  │ │    │ │ 10.244.1.0/24  10.244.1.0 fl.1  │ │
│ │ 10.244.0.0/16  0.0.0.0   fl.1   │ │    │ │ 10.244.0.0/16  0.0.0.0   fl.1   │ │
│ │ 10.0.1.0/24    0.0.0.0   eth0   │ │    │ │ 10.0.1.0/24    0.0.0.0   eth0   │ │
│ │ default        10.0.1.1  eth0   │ │    │ │ default        10.0.1.1  eth0   │ │
│ └─────────────────────────────────┘ │    │ └─────────────────────────────────┘ │
│                                     │    │                                     │
│ VXLAN FDB (Forwarding Database):    │    │ VXLAN FDB (Forwarding Database):    │
│ ┌─────────────────────────────────┐ │    │ ┌─────────────────────────────────┐ │
│ │ MAC Address       IP Address    │ │    │ │ MAC Address       IP Address    │ │
│ │ aa:bb:cc:dd:ee:02 10.0.1.101    │ │    │ │ aa:bb:cc:dd:ee:01 10.0.1.100    │ │
│ └─────────────────────────────────┘ │    │ └─────────────────────────────────┘ │
└─────────────────────────────────────┘    └─────────────────────────────────────┘
                     │                                           │
                     └─────────────────┬─────────────────────────┘
                                       │
                               Physical Network
                                (10.0.1.0/24)
</pre>

#### Packet Flow: Pod1 to Pod3 with Flannel VXLAN
Detailed Packet Capture Analysis
Scenario: Pod1 (10.244.1.10) pings Pod3 (10.244.2.10)
```
# Inside Pod1 - Original ICMP packet
tcpdump -i eth0 -nn icmp
```

Packet Structure:
<pre>
┌─────────────────────────────────────────────────────────────┐
│                    Ethernet Header                          │
├─────────────────────────────────────────────────────────────┤
│ Dst MAC: 02:42:0a:f4:01:01 (cni0 bridge)                    │
│ Src MAC: 02:42:0a:f4:01:0a (Pod1 eth0)                      │
│ EtherType: 0x0800 (IPv4)                                    │
├─────────────────────────────────────────────────────────────┤
│                      IP Header                              │
├─────────────────────────────────────────────────────────────┤
│ Version: 4, Header Length: 20 bytes                         │
│ Source IP: 10.244.1.10                                      │
│ Destination IP: 10.244.2.10                                 │
│ Protocol: 1 (ICMP)                                          │
│ TTL: 64                                                     │
├─────────────────────────────────────────────────────────────┤
│                     ICMP Header                             │
├─────────────────────────────────────────────────────────────┤
│ Type: 8 (Echo Request)                                      │
│ Code: 0                                                     │
│ Identifier: 1234                                            │
│ Sequence: 1                                                 │
│ Data: "Hello from Pod1"                                     │
└─────────────────────────────────────────────────────────────┘  
</pre>

Packet on Node A (before VXLAN encapsulation)
```
# On Node A - Capture on cni0 bridge
sudo tcpdump -i cni0 -nn icmp
# 15:30:45.123456 IP 10.244.1.10 > 10.244.2.10: ICMP echo request, id 1234, seq 1
```

VXLAN Encapsulated Packet on Physical Network
```
# On Node A - Capture on eth0 (physical interface)
sudo tcpdump -i eth0 -nn 'port 8472'
```

Encapsulated Packet Structure:
<pre>
┌─────────────────────────────────────────────────────────────┐
│                 Outer Ethernet Header                       │
├─────────────────────────────────────────────────────────────┤
│ Dst MAC: aa:bb:cc:dd:ee:02 (Node B eth0)                    │
│ Src MAC: aa:bb:cc:dd:ee:01 (Node A eth0)                    │
│ EtherType: 0x0800 (IPv4)                                    │
├─────────────────────────────────────────────────────────────┤
│                    Outer IP Header                          │
├─────────────────────────────────────────────────────────────┤
│ Version: 4, Header Length: 20 bytes                         │
│ Source IP: 10.0.1.100 (Node A)                              │
│ Destination IP: 10.0.1.101 (Node B)                         │
│ Protocol: 17 (UDP)                                          │
│ TTL: 64                                                     │
├─────────────────────────────────────────────────────────────┤
│                     UDP Header                              │
├─────────────────────────────────────────────────────────────┤
│ Source Port: 45678 (random)                                 │
│ Destination Port: 8472 (VXLAN)                              │
│ Length: 78 bytes                                            │
├─────────────────────────────────────────────────────────────┤
│                    VXLAN Header                             │
├─────────────────────────────────────────────────────────────┤
│ Flags: 0x08 (Valid VNI)                                     │
│ VNI: 1 (Virtual Network Identifier)                         │
│ Reserved: 0x000000                                          │
├─────────────────────────────────────────────────────────────┤
│                 Inner Ethernet Header                       │
├─────────────────────────────────────────────────────────────┤
│ Dst MAC: 02:42:0a:f4:02:01 (Node B cni0)                    │
│ Src MAC: 02:42:0a:f4:01:01 (Node A cni0)                    │
│ EtherType: 0x0800 (IPv4)                                    │
├─────────────────────────────────────────────────────────────┤
│                   Inner IP Header                           │
├─────────────────────────────────────────────────────────────┤
│ Version: 4, Header Length: 20 bytes                         │
│ Source IP: 10.244.1.10 (Pod1)                               │
│ Destination IP: 10.244.2.10 (Pod3)                          │
│ Protocol: 1 (ICMP)                                          │
│ TTL: 63 (decremented by 1)                                  │
├─────────────────────────────────────────────────────────────┤
│                     ICMP Header                             │
├─────────────────────────────────────────────────────────────┤
│ Type: 8 (Echo Request)                                      │
│ Code: 0                                                     │
│ Identifier: 1234                                            │
│ Sequence: 1                                                 │
│ Data: "Hello from Pod1"                                     │
└─────────────────────────────────────────────────────────────┘  
</pre>

Packet Capture Commands and Output
On Node A:
```
# Capture original packet leaving Pod1
sudo tcpdump -i veth12345678 -nn icmp -v
# 15:30:45.123456 IP (tos 0x0, ttl 64, len 84) 10.244.1.10 > 10.244.2.10: 
#   ICMP echo request, id 1234, seq 1, length 64

# Capture VXLAN encapsulated packet on physical interface
sudo tcpdump -i eth0 -nn 'udp port 8472' -v
# 15:30:45.123460 IP (tos 0x0, ttl 64, len 134) 10.0.1.100.45678 > 10.0.1.101.8472: 
#   VXLAN, flags [I] (0x08), vni 1
#   IP (tos 0x0, ttl 63, len 84) 10.244.1.10 > 10.244.2.10: 
#     ICMP echo request, id 1234, seq 1, length 64
```

On Node B:
```
# Capture incoming VXLAN packet
sudo tcpdump -i eth0 -nn 'udp port 8472' -v
# 15:30:45.123465 IP (tos 0x0, ttl 64, len 134) 10.0.1.100.45678 > 10.0.1.101.8472: 
#   VXLAN, flags [I] (0x08), vni 1
#   IP (tos 0x0, ttl 63, len 84) 10.244.1.10 > 10.244.2.10: 
#     ICMP echo request, id 1234, seq 1, length 64

# Capture decapsulated packet on cni0
sudo tcpdump -i cni0 -nn icmp -v
# 15:30:45.123470 IP (tos 0x0, ttl 63, len 84) 10.244.1.10 > 10.244.2.10: 
#   ICMP echo request, id 1234, seq 1, length 64
```

Indide Pod3
```
# Final packet received by Pod3
tcpdump -i eth0 -nn icmp -v
# 15:30:45.123475 IP (tos 0x0, ttl 63, len 84) 10.244.1.10 > 10.244.2.10: 
#   ICMP echo request, id 1234, seq 1, length 64
```

Return Path (Pod3 → Pod1)
The ICMP reply follows the reverse path:

```
# Pod3 sends reply
# 15:30:45.123480 IP 10.244.2.10 > 10.244.1.10: ICMP echo reply, id 1234, seq 1

# Node B encapsulates and sends via VXLAN
# 15:30:45.123485 IP 10.0.1.101.54321 > 10.0.1.100.8472: 
#   VXLAN, flags [I] (0x08), vni 1
#   IP 10.244.2.10 > 10.244.1.10: ICMP echo reply, id 1234, seq 1

# Pod1 receives the reply
# 15:30:45.123490 IP 10.244.2.10 > 10.244.1.10: ICMP echo reply, id 1234, seq 1
```

## Info - Calico Openshift Network Plugin
<pre>
- Calico Network in OpenShift
- Calico is a popular Container Network Interface (CNI) plugin that provides networking and 
  network security for containerized applications in OpenShift clusters.
  What is Calico?
  - Calico is an open-source networking solution that delivers:

Pod-to-pod networking across nodes
- Network policy enforcement for security
- IP address management (IPAM)
- Service mesh integration
- Key Features in OpenShift
  1. Pure Layer 3 Networking
     - Uses standard IP routing instead of overlay networks
     - Better performance with lower latency
     - Simplified troubleshooting using standard networking tools
  2. Network Policies
     - Kubernetes-native network policies
     - Advanced Calico network policies with additional features

- Ingress and egress traffic control
  - Application-layer policy enforcement
- Scalability
  - Supports thousands of nodes
  - Efficient BGP routing protocols
  - Distributed architecture without single points of failure
- Security
  - Workload isolation at the network level
  - Encryption in transit
  - Integration with service mesh security

- Architecture Components
  - Felix Agent
    - Runs on each node as a DaemonSet
    - Programs routing tables and iptables rules
    - Handles network policy enforcement

  - BGP Client (BIRD)
    - Distributes routing information between nodes
    - Maintains network topology
    - Enables cross-node pod communication

  - Calico Controller
    - Watches Kubernetes API for network policy changes
    - Translates policies into Felix-readable format
    - Manages IP address allocation  

- Common Use Cases
  - Multi-tenant environments requiring strong network isolation  
  - High-performance applications needing low network latency
  - Hybrid cloud deployments with consistent networking policies
  - Compliance requirements demanding network traffic control
</pre>

Comparison
<pre>
+------------------+----------+---------------+----------------+
| Feature          | Calico   | OpenShift SDN | OVN-Kubernetes |
+------------------+----------+---------------+----------------+
| Performance      | High     | Medium        | Medium-High    |
| Network Policies | Advanced | Basic         | Basic          |
| Complexity       | Medium   | Low           | Medium         |
| BGP Support      | Yes      | No            | No             |
| Overlay Network  | Optional | Yes           | Yes            |
| eBPF Support     | Yes      | No            | Limited        |
| IPv6 Support     | Yes      | Limited       | Yes            |
| Windows Support  | Yes      | No            | Yes            |
| Encryption       | WireGuard| IPSec         | IPSec          |
| IPAM             | Custom   | Built-in      | Built-in       |
| Troubleshooting  | Standard | OpenShift     | OVN Tools      |
|                  | Tools    | Tools         |                |
+------------------+----------+---------------+----------------+  
</pre>

## Info - Openshift Weave Network Plugin
<pre>
- Weave Network Plugin in OpenShift
  - Weave Network (Weave Net) is a Container Network Interface (CNI) plugin that 
    can be used with OpenShift to provide networking between containers and pods across a cluster.
- What is Weave Network?
  - Weave Network creates a virtual network that connects Docker containers across multiple hosts 
    and enables their automatic discovery. It provides:
- Overlay Network: Creates a Layer 2 network overlay that spans multiple hosts
- Automatic IP Management: Assigns IP addresses to containers automatically
- Service Discovery: Built-in DNS-based service discovery
- Network Segmentation: Support for network policies and microsegmentation
- Key Features in OpenShift Context
  1. Multi-Host Networking
     - Connects pods across different OpenShift nodes
     - Handles pod-to-pod communication seamlessly
     - Supports both IPv4 and IPv6
  2. Network Policies
     - Implements Kubernetes NetworkPolicy resources
     - Provides microsegmentation capabilities
     - Allows traffic filtering between pods/namespaces
  3. Encryption
    - Optional encryption of inter-host traffic
    - Uses NaCl crypto library for performance
    - Helps secure pod communication across untrusted networks
  4. Integration with OpenShift
     - Works as a CNI plugin
     - Integrates with OpenShift's Software Defined Networking (SDN)
     - Compatible with OpenShift service mesh

Architecture Components
- Weave Router
- Runs as a DaemonSet on each node
- Handles packet forwarding and routing
- Maintains network topology information
- Weave Net Plugin
  - CNI plugin that interfaces with container runtime
  - Allocates IP addresses to pods
  - Configures network interfaces
  - IPAM (IP Address Management)
  - Distributes IP address ranges across nodes
  - Prevents IP conflicts
  - Supports custom IP ranges  
</pre>

#### Usecases
- Multi-cloud deployments where encryption is required
- Strict network segmentation requirements
- Legacy applications that need specific networking features
- Development environments requiring flexible networking
- Comparison with OpenShift SDN

#### Limitations
- Performance overhead especially with encryption enabled
- Additional complexity compared to default OpenShift SDN
- Resource consumption from running additional networking components
- Troubleshooting complexity due to overlay networking
- Weave Network provides a robust networking solution for OpenShift,
  particularly useful when advanced networking features like - encryption and comprehensive
  network policies are required.
  
<pre>
+------------------------+---------------------------+---------------------------+
| Feature                | Weave Network             | OpenShift SDN             |
+------------------------+---------------------------+---------------------------+
| Overlay Technology     | VXLAN/UDP                 | VXLAN                     |
+------------------------+---------------------------+---------------------------+
| Network Policies       | Full NetworkPolicy        | Limited (requires         |
|                        | support                   | OVN-Kubernetes)           |
+------------------------+---------------------------+---------------------------+
| Encryption             | Built-in optional         | Requires additional       |
|                        | encryption                | setup                     |
+------------------------+---------------------------+---------------------------+
| Performance            | Good, with encryption     | Optimized for OpenShift   |
|                        | overhead                  |                           |
+------------------------+---------------------------+---------------------------+
| Setup Complexity       | More complex setup        | Integrated, simpler       |
+------------------------+---------------------------+---------------------------+
| IP Address Management  | Distributed IPAM          | Centralized IPAM          |
+------------------------+---------------------------+---------------------------+
| Service Discovery      | Built-in DNS              | Standard Kubernetes DNS   |
+------------------------+---------------------------+---------------------------+
| Multi-tenancy          | Network segmentation      | Project-based isolation   |
+------------------------+---------------------------+---------------------------+
| Troubleshooting        | More complex due to       | Easier with OpenShift     |
|                        | overlay networking        | tooling                   |
+------------------------+---------------------------+---------------------------+
| Resource Consumption   | Higher (additional        | Lower (integrated)        |
|                        | components)               |                           |
+------------------------+---------------------------+---------------------------+
| Cross-cluster          | Native support            | Requires additional       |
| Communication          |                           | configuration             |
+------------------------+---------------------------+---------------------------+
| Monitoring & Logging   | External tools required   | Integrated with           |
|                        |                           | OpenShift monitoring      |
+------------------------+---------------------------+---------------------------+
| Support & Maintenance  | Community/Commercial      | Red Hat supported         |
|                        | support                   |                           |
+------------------------+---------------------------+---------------------------+
</pre>

## Flannel vs Calico vs Weave
<pre>
+------------------------+---------------------------+---------------------------+---------------------------+
| Feature                | Calico                    | Flannel                   | Weave                     |
+------------------------+---------------------------+---------------------------+---------------------------+
| Overlay Technology     | BGP (L3), VXLAN, IPIP    | VXLAN, UDP, host-gw       | VXLAN, UDP                |
+------------------------+---------------------------+---------------------------+---------------------------+
| Network Policies       | Full NetworkPolicy +      | No native support         | Full NetworkPolicy        |
|                        | Calico policies           |                           | support                   |
+------------------------+---------------------------+---------------------------+---------------------------+
| Encryption             | WireGuard, IPSec          | No native encryption      | Built-in NaCl encryption  |
+------------------------+---------------------------+---------------------------+---------------------------+
| Performance            | Excellent (native BGP)    | Good (simple overlay)     | Good (with encryption     |
|                        |                           |                           | overhead)                 |
+------------------------+---------------------------+---------------------------+---------------------------+
| Setup Complexity       | Complex (BGP knowledge    | Simple                    | Moderate                  |
|                        | required)                 |                           |                           |
+------------------------+---------------------------+---------------------------+---------------------------+
| IP Address Management  | IPAM with IP pools        | Simple host-subnet        | Distributed IPAM          |
|                        |                           | allocation                |                           |
+------------------------+---------------------------+---------------------------+---------------------------+
| Service Discovery      | Standard Kubernetes DNS   | Standard Kubernetes DNS   | Built-in DNS + K8s DNS    |
+------------------------+---------------------------+---------------------------+---------------------------+
| Multi-tenancy          | Advanced policy engine    | Basic namespace           | Network segmentation      |
|                        |                           | isolation                 |                           |
+------------------------+---------------------------+---------------------------+---------------------------+
| Scalability            | Excellent (BGP routing)   | Good (limited by etcd)    | Good (mesh topology)      |
+------------------------+---------------------------+---------------------------+---------------------------+
| Cross-subnet Support   | Native (BGP)              | Limited (requires         | Good (automatic           |
|                        |                           | host-gw mode)             | discovery)                |
+------------------------+---------------------------+---------------------------+---------------------------+
| Windows Support        | Yes                       | Limited                   | No                        |
+------------------------+---------------------------+---------------------------+---------------------------+
| eBPF Support           | Yes (advanced features)   | No                        | No                        |
+------------------------+---------------------------+---------------------------+---------------------------+
| Resource Usage         | Low to moderate           | Very low                  | Moderate to high          |
+------------------------+---------------------------+---------------------------+---------------------------+
| Troubleshooting        | Complex (BGP knowledge)   | Simple                    | Moderate                  |
+------------------------+---------------------------+---------------------------+---------------------------+
| Enterprise Features    | Calico Enterprise         | Limited                   | Weave Cloud (deprecated)  |
|                        | (advanced security)       |                           |                           |
+------------------------+---------------------------+---------------------------+---------------------------+
| IPv6 Support           | Full dual-stack           | Basic                     | Yes                       |
+------------------------+---------------------------+---------------------------+---------------------------+
| Network Observability  | Flow logs, metrics        | Basic metrics             | Network map, metrics      |
+------------------------+---------------------------+---------------------------+---------------------------+
| Maturity               | Very mature               | Mature                    | Mature                    |
+------------------------+---------------------------+---------------------------+---------------------------+
| Best Use Cases         | - Large scale clusters    | - Simple deployments     | - Multi-cloud setups      |
|                        | - Advanced security       | - Learning/development    | - Encryption required     |
|                        | - Compliance requirements | - Resource constrained    | - Easy troubleshooting    |
|                        | - Multi-cloud             |   environments            | - Legacy app support      |
+------------------------+---------------------------+---------------------------+---------------------------+
| Vendor/Maintainer      | Tigera (formerly          | CoreOS/Red Hat            | Weaveworks                |
|                        | Project Calico)           |                           |                           |
+------------------------+---------------------------+---------------------------+---------------------------+
| OpenShift Support      | Yes (OVN-Kubernetes       | Limited                   | Third-party plugin        |
|                        | uses Calico for policies) |                           |                           |
+------------------------+---------------------------+---------------------------+---------------------------+
</pre>

#### Key Recommendations:
<pre>
Choose Calico when:
- You need advanced network policies and security
- Running large-scale production clusters
- Require compliance and audit capabilities
- Have networking expertise in your team

Choose Flannel when:
- You want simplicity and ease of use
- Running smaller or development clusters
- Have limited networking requirements
- Want minimal resource overhead

Choose Weave when:
- You need built-in encryption
- Running multi-cloud deployments
- Require good troubleshooting tools
- Need automatic network discovery
</pre>

## Demo - Install Red Hat AMQ Broker Operator
```
mkdir -p openshift-jms-setup/{operators,broker,queues,application}
cd openshift-jms-setup
```

Install AMQ Broker Operator - operators/amq-operator-subscription.yaml
<pre>
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  name: amq-broker-rhel8
  namespace: openshift-operators
spec:
  channel: 7.11.x
  name: amq-broker-rhel8
  source: redhat-operators
  sourceNamespace: openshift-marketplace  
</pre>

Create AMQ Broker Instance broker/amq-broker-instance.yaml
<pre>
apiVersion: broker.amq.io/v1beta1
kind: ActiveMQArtemis
metadata:
  name: amq-broker
  namespace: jms-demo
spec:
  deploymentPlan:
    size: 1
    image: registry.redhat.io/amq7/amq-broker-rhel8:7.11
    requireLogin: false
    persistenceEnabled: true
  console:
    expose: true
  acceptors:
    - name: amqp
      protocols: amqp
      port: 5672
    - name: core
      protocols: core
      port: 61616  
</pre>

Create Queues - queues/order-queue.yaml
<pre>
apiVersion: broker.amq.io/v1beta1
kind: ActiveMQArtemisAddress
metadata:
  name: order-queue
  namespace: jms-demo
spec:
  addressName: order.queue
  queueName: order.queue
  routingType: anycast  
</pre>

Create Topics - queues/notification-topic.yaml
<pre>
apiVersion: broker.amq.io/v1beta1
kind: ActiveMQArtemisAddress
metadata:
  name: notification-topic
  namespace: jms-demo
spec:
  addressName: notification.topic
  queueName: notification.topic
  routingType: multicast  
</pre>

Create Application Configuration - application/configmap.yaml
<pre>
apiVersion: v1
kind: ConfigMap
metadata:
  name: jms-app-config
  namespace: jms-demo
data:
  application.yml: |
    spring:
      activemq:
        broker-url: tcp://amq-broker-hdls-svc:61616
        user: admin
        password: admin
      jms:
        pub-sub-domain: false
    logging:
      level:
        com.example.jms: DEBUG  
</pre>



Installation script
<pre>
#!/bin/bash

echo "=== Installing JMS Application on OpenShift ==="

# Step 1: Install AMQ Broker Operator
echo "1. Installing AMQ Broker Operator..."
oc apply -f operators/amq-operator-subscription.yaml

# Wait for operator to be ready
echo "Waiting for operator to be ready..."
sleep 30

# Step 2: Create namespace and broker
echo "2. Creating namespace and AMQ Broker..."
oc new-project jms-demo --display-name="JMS Demo Application"
oc apply -f broker/amq-broker-instance.yaml

# Wait for broker to be ready
echo "Waiting for broker to be ready..."
oc wait --for=condition=Ready activemqartemis/amq-broker --timeout=300s

# Step 3: Create queues and topics
echo "3. Creating queues and topics..."
oc apply -f queues/order-queue.yaml
oc apply -f queues/notification-topic.yaml

echo "=== Installation Complete ==="
echo "Check status with: oc get pods -n jms-demo"
echo "View AMQ Console: oc get route amq-broker-wconsj-0-svc-rte -n jms-demo"  
</pre>


Make the script executable
```
chmod +x install.sh
./install.sh

# Access AMQ Console
oc get routes -n jms-demo
```

## Info - Openshift S2I Overview
<pre>
- In Kubernetes, we can deploy applications only using container images
- In Openshift, we can deploy applications
  - using container image
  - using GitHub, BitBucket, Gilab etc url that has your application source code (S2I)
- Openshift supports many S2I strategies
  - Docker
  - Source
  - Build
  - Custom
  - Pipeline ( Jenkinsfile )
</pre>

## Info - Openshift S2I Docker Strategy Overview
<pre>
- Source code from any version control can be used
- In our case, we will be using GitHub, hence our repository must be have the below for S2I Docker strategy
  - application source code
  - Dockerfile
- With the GitHub url, branch details, Openshift will generate a BuidConfig 
- Using the BuildConfig, Openshift Build Controller will create Build (Pod), 
  - clones your source code
  - follows the instructions mentioned in your Dockerfile
  - builds your application to create applicaiton executable
  - builds a custom container image with your applicaiton executable configured as default application
  - Pushes the image to Openshift's Internal Image Registry
  - It deploys the applicaiton using the custom image pushed recently into the Internal Image Registry
  - It creates a service for the deployed applicaton
</pre>

## Info - Openshift S2I Source Strategy Overview
<pre>
- Source code from any version control can be used
- In our case, we will be using GitHub, hence our repository must be have the below for S2I source strategy
  - application source code
- With the GitHub url, branch details, Openshift will generate a BuidConfig and a Dockerfile
- Using the BuildConfig, Openshift Build Controller will create Build (Pod), 
  - clones your source code
  - follows the instructions mentioned in your Dockerfile
  - builds your application to create applicaiton executable
  - builds a custom container image with your applicaiton executable configured as default application
  - Pushes the image to Openshift's Internal Image Registry
  - It deploys the applicaiton using the custom image pushed recently into the Internal Image Registry
  - It creates a service for the deployed applicaton 
</pre>

## Lab - Deploying your application into Openshift using S2I docker strategy
```
oc delete project jegan
oc new-project jegan

oc new-app --name=hello https://github.com/tektutor/spring-ms.git --strategy=docker
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/962ac91c-203f-4c14-a146-3e6cdb0d9c5b" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/9cfd3631-9aaa-4b4f-8976-2df3ca3cf0e7" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/39d340e5-b52b-4fdd-ba42-f633e6453a01" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f63a0ede-6532-43b2-a0f3-97dd22828a73" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/276b53ea-ed03-4837-a440-2db1febc5bc4" />

## Lab - Deploying your application into Openshift using S2I source strategy
```
oc delete project jegan
oc new-project jegan

oc new-app --name=hello registry.access.redhat.com/ubi8/openjdk-17~https://github.com/tektutor/spring-ms.git --strategy=source
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/d6393095-ffb5-444a-beb5-37dac442e758" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/b8f54331-dc35-4bd2-9472-10d7b474e680" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/fcd27cf2-4482-4222-9e17-45463432a32c" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/d8cbfd2e-4f35-4882-970d-16cdc8cfc38a" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/39c51fbe-3fb3-4e20-8c6f-8d6e4a8a00f7" />

## Demo - CI/CD Demo

Download Jenkins
```
cd ~
wget https://get.jenkins.io/war-stable/2.516.1/jenkins.war
```

Launch Jenkins CI Build Server ( You won't be able to use the terminal where you launched jenkins, you could use different terminal window/tab )
```
java -jar ./jenkins.war
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/de0845c9-4327-44a7-92b1-939be7219b98" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/fb5da3c1-8e7e-4399-b5b5-a722c836a425" />

You may access the Jenkins dashboard from chrome web browser
```
http://localhost:8080
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/1fe1515d-8b09-45bd-9d56-e4fedf373476" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f477d98c-15f1-4734-9c02-24e8a99e75bb" />
Click "Continue" button
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/65971040-df7c-40e3-8c08-2551afd890c8" />
Click "Install suggested plugins"
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/43814c3d-9426-4bb1-ab69-637ae717ec79" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/a85a9f39-743d-4125-acc9-21ae3b6131be" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/32fde0d5-2f8a-45be-9fcc-3606654453ce" />
Make sure you create an admin user
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/541b515f-56eb-4fc5-b0e1-513ab67bb2a7" />
Click "Save and Finish"
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/2d9a19a2-f9af-4c98-8452-636878bb8db9" />
Click "Start using Jenkins"
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/41bacd85-0518-435d-9957-f83351521760" />

Create a Job, select "Freestyle Job"
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/9d597891-bccb-4763-acf9-383e2f2d23c6" />
Click "Ok"

In Source Code Management Section, select "Git"
<pre>
Reposistory url - https://github.com/tektutor/openshift-aug-2025.git
Branches to Build - */main
</pre>
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/91cab130-1248-4124-97ac-d7b708af655a" />

In Triggers section,
<pre>
Select "Poll SCM"
Schedule "H/02 * * * *"
</pre>
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/0ae17124-a096-4859-8e70-246fc5a33403" />

In Build Steps section,
<pre>
Click "Add Build Step"
Select "Execute Shell" 
</pre>
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/2e5db47d-3913-49a2-8a60-7bb51c0baba5" />

Click "Save"
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/c007ec17-6e6d-4954-bff9-ffba6d4403b6" />


## Lab - JMS Producer and JMS Consumer

<pre>
What is JMS (Java Message Service)?
- JMS is a Java API that allows applications to create, send, receive, and read messages. 
- It provides a way for Java applications to communicate asynchronously through messaging.
- In this example, we will be using Apache ActiveMQ Artemis to support 

About Apache ActiveMQ Artemis 
- is a high-performance, enterprise-grade message broker that implements 
  the Java Message Service (JMS) 2.0 specification. 
- it is the next-generation message broker from the Apache ActiveMQ project, 
  built from the ground up for high performance and scalability.
- Key Features
  - High Performance
  - Non-blocking I/O architecture
  - Zero-copy message handling
  - Handles millions of messages per second
  - Low latency message delivery
  - Efficient memory management
- Enterprise Grade
  - ACID transactions support
  - High Availability with clustering
  - Message persistence to disk
  - Security integration (JAAS, LDAP, SSL/TLS)
  - Management console and JMX monitoring
- Protocol Support
 - JMS 1.1 & 2.0 (primary)
 - AMQP 1.0 (Advanced Message Queuing Protocol)
 - STOMP (Simple Text Oriented Messaging Protocol)
 - MQTT (Message Queuing Telemetry Transport)
 - OpenWire (ActiveMQ Classic compatibility)
  
Key Uses of JMS
1. Asynchronous Communication
   - Applications can send messages without waiting for immediate responses
   - Decouples sender and receiver - they don't need to be online simultaneously
   - Improves application responsiveness and scalability
 
2. Microservices Integration
   - Enables communication between different microservices
   - Services can operate independently and communicate via messages
   - Supports event-driven architecture patterns
 
3. Enterprise Application Integration (EAI)
  - Connects different enterprise systems and applications
  - Enables legacy system integration with modern applications 
  - Facilitates data exchange between heterogeneous systems
 
4. Load Distribution and Scalability
   - Distributes workload across multiple consumers
   - Supports horizontal scaling by adding more message consumers
   - Handles traffic spikes through message queuing
</pre>

I have already installed Red Hat Integration - AMQ Broker for RHEL(mutiarch) Operator required for this exercise under jms-demo project. Hence, you don't have to install it.
Login Credentials
<pre>
username : jegan
password : root@123
</pre>

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/5bb75173-d256-49e6-b589-af0255c8a850" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/6ec442b1-709e-4f28-babf-4e810575b677" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/978828a7-d95a-41f1-abca-145fb9563f83" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/22a9b442-f668-423d-ad84-57b9028efb9a" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/13733724-90b3-4ca8-894a-9c0564c0c0cd" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/f85015ab-0d14-44d7-ad98-51120b525cc8" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/79e95432-826e-4f1f-826c-c2fe4a0c6c5a" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/19cfe442-0ed7-4b68-8f69-9d4ca08864ae" />


You can run the application from your project namespace.

```
oc delete project jegan
oc new-project jegan

# Deploy your JMS producer application
oc new-app java:openjdk-17-ubi8~https://github.com/tektutor/openshift-aug-2025.git \
  --context-dir=Day5/jms-demo/producer \
  --name=jms-producer

# Check the S2I source strategy build progress, wait until it reports Image pushed successfully
oc logs -f bc/jms-producer

# Wait until your jms-producer starts running
oc get pods

# You can check the logs of your JMS Producer
oc logs -f jms-producer-586c48df6f-9rvwj
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/3a34748f-2222-4af3-9a4b-b5d0ecafe60e" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/634ecd60-6fdb-4aef-9510-995bd32db4ef" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/ab5e502e-430d-4dbc-b325-077c2280d887" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/69c408d7-584c-44ed-9cb2-2acbf13fc0fe" />

In a different terminal window/tab, try this
```
oc project jegan

oc new-app java:openjdk-17-ubi8~https://github.com/tektutor/openshift-aug-2025.git \
  --context-dir=Day5/jms-demo/consumer \
  --name=jms-consumer

# Check the S2I source strategy build status, wait until you see Image Pushed successfully message
oc logs -f bc/jms-consumer

# Wait until your jms-producer starts running
oc get pods

oc logs -f jms-consumer-78d69685d9-hcnk8
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/e3ca3b17-60ec-40b4-a9de-d020b088cf2e" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/ec26309f-a064-4dbb-86a7-4a0ec7aea90b" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/ba7a0317-da73-47a1-bda6-a92ac1dd64c8" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/e028120d-d0cf-43c3-bd15-a392b35fbab9" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/83a3bccf-8096-46d2-aa2b-8ab03103bd6f" />

You can also check the JMS Producer and Consumer logs from Openshift webconsole
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/fcb53481-de45-4e51-b03e-ce54c067597d" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/1596ea2a-6b0d-40b3-80b6-742c300a6e67" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/187682b6-18f9-4339-97cd-195f1a7b4d31" />


<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/8c9b7ee0-6f51-420b-b4bb-1703cc887c1d" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/9d424ed8-fd97-4fc2-a4e2-1d5212b6c713" />
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/a22f1aa2-9f9e-4169-b50a-cf53a1d1aefa" />
