import java.util
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerBestFit, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.CollectionConverters.*
import scala.jdk.javaapi.CollectionConverters.asJava


/**
 * Simulation 1 : SpaceShared
 */

class Simulation1

object Simulation1 {
  val SIM = "simulation1config"
  val conf: Config = ConfigFactory.load(SIM + ".conf")

  val HOSTS: Int = conf.getInt(SIM + "." + "datacenter" + ".numHosts")
  val HOST_PES: Int = conf.getInt(SIM + "." + "host" + ".pes")
  val VMS: Int = conf.getInt(SIM + "." + "numVMs")
  val VM_PES: Int = conf.getInt(SIM + "." + "vm" + ".pesNumber")
  val CLOUDLETS: Int = conf.getInt(SIM + "." + "numCloudlets")
  val CLOUDLET_PES: Int = conf.getInt(SIM + "." + "cloudlet" + ".pesNumber")
  val CLOUDLET_LENGTH: Int = conf.getInt(SIM + "." + "cloudlet" + ".length")
  val utizationRatio = conf.getDouble(SIM  + ".utizationRatioSpaceShared")

  val LOG: Logger = LoggerFactory.getLogger(getClass)

  val simulation = new CloudSim
  val broker0 = new DatacenterBrokerSimple(simulation)

  LOG.info("Datacenters are going to be created.")
  val datacenter: DatacenterSimple = createDatacenter(HOSTS)
  LOG.info("Creating VMS")
  val vmList: util.List[Vm] = createVms
  LOG.info("Creating Cloudlets")
  val cloudletList: util.List[CloudletSimple] = createCloudlets

  LOG.info("Sumbiting VM List to broker")
  broker0.submitVmList(vmList)
  LOG.info("Sumbiting cloudlet List to broker")
  broker0.submitCloudletList(cloudletList)
  LOG.info("Starting Simulation.")
  simulation.start
  
  val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList
  new CloudletsTableBuilder(finishedCloudlets).addColumn(new TextTableColumn("Actual CPU Time"), (cloudlet: Cloudlet) =>  BigDecimal(cloudlet.getActualCpuTime).setScale(3, BigDecimal.RoundingMode.HALF_UP)).addColumn(new TextTableColumn("Total Cost"), (cloudlet: Cloudlet) =>  BigDecimal(cloudlet.getTotalCost).setScale(3, BigDecimal.RoundingMode.HALF_UP)).build()
  LOG.info("The Simulation has ended.")

  /**
   * Creates a Datacenter and its Hosts.
   */
  def createDatacenter(numHosts: Int): DatacenterSimple = {
    val hostList_new = (1 to numHosts).map(host => createHost).toList

    val dc = {
      new DatacenterSimple(simulation, hostList_new.asJava, new VmAllocationPolicyBestFit)
    }
    dc.getCharacteristics
      .setCostPerBw(conf.getInt(SIM + "." + "datacenter" + ".costPerBw"))
      .setCostPerMem(conf.getInt(SIM + "." + "datacenter" + ".costPerMem"))
      .setCostPerSecond(conf.getInt(SIM + "." + "datacenter" + ".cost"))
      .setCostPerStorage(conf.getInt(SIM + "." + "datacenter" + ".costPerStorage"))
    dc
  }

  /**
   * Creates a list of Hosts with its PEs
   */
  def createHost: Host = {

    val peList = (1 to HOST_PES).map(pe => new PeSimple(conf.getInt(SIM + "." + "host" + ".mips"))).toList
    val ram = conf.getInt(SIM + "." + "host" + ".ram")
    val bw = conf.getInt(SIM + "." + "host" + ".bw")
    val storage = conf.getInt(SIM + "." + "host" + ".storage")
    new HostSimple(ram, bw, storage, asJava[Pe](peList)).setVmScheduler(new VmSchedulerSpaceShared())
  }

  /**
   * Creates a list of VMs.
   */
  def createVms: util.List[Vm] = {
    val list = (1 to VMS).map(vm => {
      val vm = new VmSimple(conf.getInt(SIM + "." + "vm" + ".mips"), VM_PES)
        .setCloudletScheduler(new CloudletSchedulerSpaceShared())
      vm.setRam(conf.getInt(SIM + "." + "vm" + ".ram"))
        .setBw(conf.getInt(SIM + "." + "vm" + ".bw"))
        .setSize(conf.getInt(SIM + "." + "vm" + ".size"))
      vm
    }).toList
    list.asJava
  }




  /**
   * Creates a list of Cloudlets.
   */
  def createCloudlets: util.List[CloudletSimple] = {
    // Uses the Full Utilization Model meaning a Cloudlet always utilizes a given allocated resource from its Vm at 100%, all the time.
    val utilizationModel = new UtilizationModelDynamic(utizationRatio)
    val list = (1 to CLOUDLETS).map(c => new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel)).toList
    list.asJava
  }

  def main(args: Array[String]): Unit = {
    Simulation1
  }
}

