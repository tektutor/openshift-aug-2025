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

## Packet Flow: Pod1 to Pod3 with Flannel VXLAN
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
