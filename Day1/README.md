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

## Info - Containerization
<pre>
- is an application virtualization technology
- each container represents a single application process in the OS
- contatiners don't represent an OS, they are just a single application process
- there are similarities between a virtual machine and containers
  - each container get its own IP Address just like Operating Systems get their own IP address
  - each container get its own NIC (Network Card), can have one or more network card ( software defined - virtual )
  - each container gets its own network stack ( 7 OSI Layers )
  - each container get its own file sytem
  - each containers get its own port range ( 0 to 65535 )
- it is for these reasons, people tend to compare a container with a virtual machine
- each container runs in a separate namespace
- containers are isolated from each other via namespaces (Linux kernel feature)
- we can apply resource quota restrictions by using Linux kernel feature called Control Groups ( CGroups ) 
- containers has
  - application and its dependencies
</pre>

## Info - Container Runtime Overview
<pre>
- is a low-level software that helps us manage container images and containers
- it is not user-friendly, hence normally end-users like us never use it directly
- examples
  - runC
  - cRun
  - CRI-O
</pre>

## Info - Container Engine Overview
<pre>
- is a high-level software that helps us manage container images and containers
- it is a user-friendly, end-user normally use this
- examples
  - Docker - internally depends on containerd, which in turn depends on runC
  - Podman - internally depends on CRI-O container runtime
  - Containerd - internally depends on runC container runtime
</pre>

## Info - Docker Overview
<pre>
- is a container engine developed in Go language by a company called Docker Inc
- it follows client/server architecture
- it helps run containerized application workloads
- the client tool is called docker
- the server tool is called dockerd, this runs a service with admin privilege
- wherever the Docker Server is running, it creates a local docker registry
- local docker registry
  - it is folder maintained on the system level for all users
  - in case of linux, /var/lib/docker where all docker images are maintained
- private docker registry 
  - it is server that can be setup optionally
  - generally in the industry they use Sonatype Nexus or JFrog Artifactory
  - it hosts multiple docker images
- remote docker registry ( aka Docker Hub website )
  - it hosts multiple docker images
  - it is maintained by Docker Inc with support from many opensource community contributors
</pre>

## Info - Docker Image
<pre>
- is a blueprint of a container
- it is similar to windows.iso or ubuntu.iso file
- whatever software, libraries and dependencies your application has, everything can be installed, configured in the docker image
- using docker image, mutiple containers can be created
- though some of the docker images may sound/appear like a operating system, they are not really OS
  - they just have package managers, commonly used linux tools, bash/sh terminals etc.,
</pre>

## Info - Docker Container
<pre>
- running instance of a Docker Container
- containers gets atlease one IP address and one Network card
- containers normally has their on terminal ( bash or shell )
- certain docker images also comes with pre-installed, pre-configured application ( default application that runs when containers are created )
</pre>
