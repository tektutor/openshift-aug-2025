# Day 5

## Flannel Network Model
<pre>
Flannel is a simple and easy way to configure a layer 3 network fabric designed for Kubernetes. It provides networking for container clusters by creating a virtual network that spans across all nodes.

Key Concepts
Overlay Network
Creates a virtual network layer on top of the existing host network
Each node gets a subnet from a larger cluster network
Pods can communicate across nodes using this overlay
Network Backends
VXLAN (Default)

Encapsulates packets in UDP
Works across most network infrastructures
Good performance with modern kernels
Host-Gateway

Uses host routing tables
Better performance but requires Layer 2 connectivity
No packet encapsulation overhead
UDP

Legacy backend
User-space packet forwarding
Lower performance but maximum compatibility
</pre>

Architecture
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
