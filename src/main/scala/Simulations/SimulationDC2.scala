package Simulations

import java.util
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
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
 * SimulationDC2  : with two data center
 */

object SimulationDC2 {
  val SIM = "simulation2config"
  val conf: Config = ConfigFactory.load(SIM + ".conf")

  val DcCount: Int = conf.getInt(SIM + "." + "numberDC")
  val utizationRatio = conf.getDouble(SIM + "." + "utizationRatio")
  val VMS: Int = conf.getInt(SIM  + ".vm" + ".numberVM")
  val VM_PES: Int = conf.getInt(SIM + ".vm" + ".numberPES")
  val CLOUDLETS: Int = conf.getInt(SIM + ".cloudlet" + ".numCloudlets")
  val CLOUDLET_PES: Int = conf.getInt(SIM + ".cloudlet" + ".numberPES")
  val CLOUDLET_LENGTH: Int = conf.getInt(SIM + ".cloudlet" + ".length")

  val LOG: Logger = LoggerFactory.getLogger(getClass)
  val simulation = new CloudSim
  val broker = new DatacenterBrokerSimple(simulation)
  LOG.info("Datacenters are going to be created.")
  val datacenter  = (1 to DcCount).map(DC => createDatacenter("datacenter" + DC))
  LOG.info("Creating VMS")
  val vmList: util.List[Vm] = createVms
  LOG.info("Creating Cloudlets")
  val cloudletList: util.List[CloudletSimple] = createCloudlets
  LOG.info("Sumbiting VM List to broker")
  broker.submitVmList(vmList)
  LOG.info("Sumbiting cloudlet List to broker")
  broker.submitCloudletList(cloudletList)
  LOG.info("Starting Simulation.")

  simulation.start

  val finishedCloudlets: util.List[Cloudlet] = broker.getCloudletFinishedList
  new CloudletsTableBuilder(finishedCloudlets).addColumn(new TextTableColumn("Actual CPU Time"), (cloudlet: Cloudlet) =>  BigDecimal(cloudlet.getActualCpuTime).setScale(3, BigDecimal.RoundingMode.HALF_UP)).addColumn(new TextTableColumn("Total Cost"), (cloudlet: Cloudlet) =>  BigDecimal(cloudlet.getTotalCost).setScale(3, BigDecimal.RoundingMode.HALF_UP)).build()
  LOG.info("The Simulation has ended.")

  /**
   * Creates a Datacenter and its Hosts.
   */


  def createDatacenter(datacenter : String): DatacenterSimple = {
    val numHosts: Int = conf.getInt(SIM + "." + datacenter + ".numberHosts")
    val hostList_new = (1 to numHosts).map(host => createHost(datacenter)).toList

    //simple will make a diff
    val dc = new DatacenterSimple(simulation, hostList_new.asJava, new VmAllocationPolicyBestFit)
    dc.getCharacteristics
      .setCostPerBw(conf.getInt(SIM + "." + datacenter + ".costPerBw"))
      .setCostPerMem(conf.getInt(SIM + "." + datacenter + ".costPerMem"))
      .setCostPerSecond(conf.getInt(SIM + "." + datacenter + ".cost"))
      .setCostPerStorage(conf.getInt(SIM + "." + datacenter + ".costPerStorage"))
    dc
  }

  /**
   * Creates a list of Hosts with its PEs
   */
  def createHost(datacenter : String) : Host = {
    val HOST_PES: Int = conf.getInt(SIM + "." + datacenter + ".host"+ ".pes")
    val peList = (1 to HOST_PES).map(pe => new PeSimple(conf.getInt(SIM + "." + datacenter + ".host"+ ".mips"))).toList
    val ram = conf.getInt(SIM + "." + datacenter + ".host"+ ".ram")
    val bw = conf.getInt(SIM + "." + datacenter + ".host"+ ".bw")
    val storage = conf.getInt(SIM + "." + datacenter + ".host"+ ".storage")
    new HostSimple(ram, bw, storage, asJava[Pe](peList)).setVmScheduler(new VmSchedulerTimeShared())
  }

  /**
   * Creates a list of VMs.
   */


  def createVms: util.List[Vm] = {
    val list = (1 to VMS).map(vm => {
      val vm = new VmSimple(conf.getInt(SIM + "." + "vm" + ".mips"), VM_PES)
        .setCloudletScheduler(new CloudletSchedulerSpaceShared)
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
    SimulationDC2
  }
}

