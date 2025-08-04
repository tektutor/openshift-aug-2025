# Day 1

## Info - Boot Loaders
<pre>
- Boot Loader is a system utility that gets installed on the Sector 0, Byte 0 of your Hard Disk (MBR)
- MBR stands for Master Boot Record
- In the Master Boot Record the Boot Loader software is installed as part of your OS installation
- commonly referred as dual/multi-booting
- examples
  - LILO ( Linux Loader - almost dead and replaced by GRUB )
  - GRUB2 
  - BootCamp ( mostly in Mac Books )
- here only one OS can be active at any point of time
</pre>  

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
- many OS can run side by side, i.e more than OS can be actively running
</pre>

