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
┌─────────────────┐    ┌─────────────────┐
│    Node A       │    │    Node B       │
│  10.244.1.0/24  │    │  10.244.2.0/24  │
│                 │    │                 │
│ ┌─────┐ ┌─────┐ │    │ ┌─────┐ ┌─────┐ │
│ │Pod1 │ │Pod2 │ │    │ │Pod3 │ │Pod4 │ │
│ └─────┘ └─────┘ │    │ └─────┘ └─────┘ │
│                 │    │                 │
│   flannel0      │    │   flannel0      │
└─────────────────┘    └─────────────────┘
         │                       │
         └───────────────────────┘
              10.244.0.0/16  
</pre>
