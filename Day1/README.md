# Day 1

## Info - Hypervisor Overview
<pre>
- Hypervisor is nothing but virtualization technology
- with virtualization we can run multiple OS at the same time in a laptop/desktop/workstation/server
- there are two types of Hypervisors
  1. Type 1
     - this is used in Server & Workstations
     - is a.k.a bare metal Hypervisor
     - doesn't require Host OS 
     - examples
       - VMWare vSphere/vCenter
       - KVM ( opensource and supported in all Linux Distributions )
  2. Type 2
     - this is used in Laptops, Desktops & Workstations
     - this Hypervisor software is installed on top of Host OS ( Windows, Mac OS-X or Linux OS )
     - examples
       - VMWare Workstation ( Windows & Linux )
       - VMWare Fusion ( Mac OS-X )
       - Oracle VirtualBox ( Windows, Mac OS-X & Linux )
       - Microsoft Hyper-V ( Windows )
       - Parallels ( Mac OS-X )
- this type of virtualization is called heavy-weight virtualization
- the Operating System that runs within the Virtual Machine(VM) is called Guest OS
- For each VM, we need to allocated dedicated hardware resources
  - CPU Cores
  - RAM
  - Storage
- each VM represents one fully functional Operating System
</pre>
