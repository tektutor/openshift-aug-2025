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
- comes in 2 flavours
  1. Docker Community Edition - Docker CE ( opensource )
  2. Docker Enterprise Edition - Docker EE ( Paid Enterprise product)
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

## Info - Hypervisor High-Level Architecture
![Hypervisor](HypervisorHighLevelArchitecture.png)

## Info - Docker High-Level Architecture
![Docker](DockerHighLevelArchitecture.png)

## Demo - Installing Docker Community Edition in Ubuntu
```
# Add Docker's official GPG key:
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update

sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl status docker

sudo usermod -aG docker $USER
docker --version
docker images
```

## Lab - Checking the docker version
```
docker --version
docker info
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/5dca0f7c-a352-4fa4-91ef-3b51422df7ea" />


## Lab - Listing docker images from your local docker registry
```
docker images
```
<img width="1960" height="624" alt="image" src="https://github.com/user-attachments/assets/3e7b0d54-86b2-46b2-aab4-021b224ead26" />

Troubleshooting permission denied error, when it prompts for password type palmeto@123
```
id
su <your-user-name>
id
docker images
```

## Lab - Download docker image from Docker Hub Remote Registry
```
docker images
docker pull ubuntu:latest
docker images
```
<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/a782d109-5d2f-4df9-9cea-d4ab2e6e3035" />


## Lab - Deleting a docker image from Docker Local Registry ( var/lib/docker )
```
docker images | grep hello-world
docker rmi hello-world:latest
docker images | grep hello-world
```

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/129b7efa-b89c-487b-bf84-3976d3d5d24f" />

## Lab - Creating a docker container in interactive(foreground) mode
```
docker run -it --name ubuntu-jegan --hostname ubuntu-jegan ubuntu:latest /bin/bash
```

<pre>
- In the above command, docker is the client tool that helps us interact with the docker server
- run - command creates a new container and starts the container
- it - interactive terminal, runs in in foreground/interactive mode
- name - name of the docker or container id, this is what docker server uses internally
- hostname - used as an alternate to IP address just like we assign a hostname for virtual machine or OS
- ubuntu:latest - this is the name docker image, latest is the tag/version of the ubuntu image
- /bin/bash - this launchest the bash shell terminal inside the container as the default application
</pre>

<img width="1920" height="1168" alt="image" src="https://github.com/user-attachments/assets/2f2e2c7e-c41b-4f34-a83b-c8bb33a5d673" />

## Lab - List all currenly running containers
```
docker ps
```

## Lab - List all containers irrespective of whether they running, exited or just created
```
docker ps -a
```

