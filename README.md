# Homework 1
### Create cloud simulators in Scala for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.

##How to run
Please follow the following steps to run the simulations implemented as part of this homework -. RECOMMENDED IntelliJ IDEA with the Scala plugin installed along with sbt.
1) Open IntellJ IDEA, in the  welcome screen select “Check out from Version Control” and then “Git”.
2) Enter the following URL and click “Clone”: https://github.com/RamiyaShreeSeshaiah/CloudSimulator-441.git
3) When prompted click “Yes” in the dialog box
4) The SBT import screen will appear, so proceed with the default options and click on “OK”
5) Confirm overwriting with “Yes”
6) Please go to src/main/scala/simulations and start running the simulation of your choice. An IntelliJ run configuration is auto-created when you click the green arrow next to the main method of the simulation file you want to run.

####: How to run in command line using Sbt
* git clone https://github.com/RamiyaShreeSeshaiah/CloudSimulator-441.git
* cd CloudSimulator-441  
* sbt clean compile test

##Project Structure

### Simulations

Simulations can be found under (src/main/scala/simulations) 

The following are Simulation classes written in Scala using CloudSim Plus Framework are provided -

- Simulation
- Simulation1
- Simulation2
- SimulationDC2

### Tests

Tests can be found under (src/main/test/scala/simulations)

The following test classes are provided: Simulation1Test

<img src=".image/sim1.PNG" alt="drawing" width="600" height="600"/>

The following methods are tested:

 1) createDatacenter: the test checks that the created Datacenter is not null and that it contains the correct number of hosts.
 2) createDatacenters: the test checks that upon a recursive call to this method the right number of datacenters is created.
 3) createHostImpl: the test checks that upon a recursive call to this method the right number of hosts is created.
 4) createVM: the test checks that upon a recursive call to this method the right number of VMs is created.
 5) createCloudlet: the test checks that upon a recursive call to this method, the cloudlets created are of the right type and number.

### Configuration
Resources can be found under (src/main/resources)
This folder mostly contains the various configuration files for the simulation that was generated. 

- simulationconfig
- Simulation1config
- SimulationDcconfig

The following entities are the configuration parameters:
- Datacenter : The characteristics of DC are architecture, operating system, virtual machine manager, number of host and every datacenter has a pricing model defining
  Cost per MB of Memory, Cost per MB of Storage, Cost per Megabit of Bandwidth, Cost per Second of CPU Use
- Host : The characteristics are  Million Instruction Per Second(MIPS), RAM, storage, bandwidth, and processing elements
- Virtual Machine : The characteristics are  Million Instruction Per Second(MIPS), virtual machine manager, Size, RAM, storage, bandwidth, and processing elements
- Cloudlet : Number of Cloudlets, Length and the Processing element.
  Simulations Policies

##Results Analysis

In this section, I will discuss the simulation outcomes that were analyzed.

### Simulation1 (SpaceShared) and Simulation2 (TimeShared)
I used total cost as my metric to compare simulation results in Simulation1 and Simulation2. Because one of the most significant factors in cloud computing is cost, it allows us to choose from a range of cloud architectures.

The total cost of the simulations are as follows:


- Simulation1 : 10.00 cost units (SpaceShared)

- Simulation2 : 33.343 cost units (TimeShared)

The generated results can be found in .results file

When examining each cloudlet in greater detail, it becomes clear that the cost in a TimeShared environment is larger than in a SpaceShared context.


The reason behind this is that : In TimeShared, multiple cloudlets are assigned to the same VM and run at the same time, resulting in a greater cost. Cloudlets in SpaceShared share VMs. On a VM, only one cloudlet is active at a time.

One more interesting observation in time and shared spacing with relation to resource utlization is Utilization Model, which outlines how a cloudlet can use a VM resource.
Various Utilization Models are listed below.

- UtilizationModelFull: It provides 100 percent resource utilization, as the name implies.
- UtilizationModelDynamic: It is possible to set the percentage of resource utilization here.

I made this observation when logs displayed “CloudletSchedulerSpaceShared: Cloudlet 15 requested 512 MB of RAM but no amount is available,”. This is when I understood how the Utilization Model plays a part in resource demand and availability.
When using the UtilizationModelFull, one VM is totally dedicated to a Cloudlet, resulting in more VM as the number of Cloudlets grows. When the availability falls short of the criteria, the above log is generated. In this case, the cloudlet will wait till the resource is freed. For efficient resource utilization across cloudlets.
the UtilizationRatio for UtilizationModelDynamic can be written as (1 / number of cloudlets)

### SimulationDC : This Simulation has two DataCenter

With two datacenters the different allocation patterns can be observed in VMAllocationPolicy and DataCenterBrokerPolicy.

VMAllocationPolicy defines how each VM is allocated to physical machine(list of PEs in Host). The different VMAllocationPolicies ares:

- VMAllocationPolicyBestFit : Allocates VM to the Host which has the maximum number of resources that are enough for VM

- VMAllocationPolicyFirstFit : Allocates the VM to the first Host with appropriate resources for the VM.
 
- VMAllocationPolicySimple : Allocates VM to the Host which has the fewest resource in use in a linear fashion

- VMAllocationPolicyRoundRobin:  In a circular fashion, allocates VM to the Host who has the appropriate resources for VM.

DataCenterBrokerPolicy defines how Cloudlets are sent to different VMs in different Host.

The different policy in DataCenterBroker are:

- DatacenterBrokerSimple : Sends cloudlets to the VM with the fewest resources in use in linear approach,

- DatacenterBrokerBestFit : Many cloudlets can be supplied to a VM if it has enough resources to run them. The cloudlet is only moved to the next VM if the current VM is unable to run it.DatacenterBrokerFirstFit : Sends cloudlets to the first VM which can run the cloudlet.
- DatacenterBrokerFirstFit : Sends cloudlets to the first appropriate VM

#### Simulation:

Various services are implemented in this simulation. 

In this simulation, I have fixed simulation characteristics that users cannot choose from; on the other hand, a list of available possibilities is randomly selected as the user does.

A service(IAAS, PAAS, SAAS, and FAAS) is randomly picked. Based on the service selected certain parameters are selected randomly and the others are fixed.

The following are characteristics that are randomly selected under each service. User have choice to select these characteristics under each service 

- IAAS -> cloudlet length, number of cloudlets, number of cloudlet PEs, and Operating Systems
- PAAS -> cloudlet length, number of cloudlets, number of cloudlet PEs 
- SAAS -> cloudlet length, number of cloudlets, number of cloudlet PEs  
- FAAS -> cloudlet length

Once these parameters are picked. The simulation is performed 




