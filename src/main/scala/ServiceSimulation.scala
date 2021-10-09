import java.util
import com.typesafe.config.{Config, ConfigFactory, ConfigList}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicy, VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletScheduler, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmScheduler, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.CollectionConverters.*
import scala.jdk.javaapi.CollectionConverters.asJava
import scala.math.random

/**
 * Implementation of SAAS,PAAS,IAAS,FAAS with 2 datacenter. 
 * User choice is randomized here. Based on the service the characteristics value are picked
 *
 */

object ServiceSimulation {

  val SIM = "simulationconfig";
  val conf: Config = ConfigFactory.load(SIM + ".conf")

  // The default configurations -> user has no control over these values
  val dc: Int = conf.getInt(SIM + ".numberDC")
  val VMS: Int = conf.getInt(SIM  + ".numVMs")
  val VM_PES: Int = conf.getInt(SIM + "." + "vm" + ".pesNumber")
  val vmMips = conf.getInt(SIM + "." + "vm" + ".mips")
  val utizationRatio = conf.getDouble(SIM +  ".utizationRatio")
  val numberOfCloudletsList = conf.getList(SIM + "." + "numCloudlets")
  val cloudletLengthList = conf.getList(SIM + "." + "cloudlet" + ".length")
  val cloudletPEsList = conf.getList(SIM + "." + "cloudlet" + ".numberPES")
  val operatingSystemList = conf.getList(SIM + "." + "datacenter1" + ".os")

  //picked by user in all services -> randomly selected here
  val cloudletLength = (randomValue(cloudletLengthList, randomNumber))

  //Random selection of service
  val service : List[String] = List("IAAS", "PAAS", "SAAS", "FAAS")
  val serviceName = service(randomNumber)

  val LOG: Logger = LoggerFactory.getLogger(getClass)

  val simulation = new CloudSim
  val broker = new DatacenterBrokerSimple(simulation) //Simple Broker is used here
  
  LOG.info(dc + "Datacenters are going to be created.")
  val datacenter = (1 to dc).map(DC => createDatacenter("datacenter" + DC))

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

    LOG.info("Creating" + numHosts + "hosts")

    val hostList_new = (1 to numHosts).map(host => createHost(datacenter)).toList

    //here based on the service the OS is assigned
    
    val dc = new DatacenterSimple(simulation, hostList_new.asJava, new VmAllocationPolicyFirstFit)
    dc.getCharacteristics
      .setOs(  if(serviceName == "IAAS"){
        (randomValueStr(operatingSystemList, randomNumber))}
      else{
        (randomValueStr(operatingSystemList, 0))
      })
      .setCostPerBw(conf.getInt(SIM + "." + datacenter + ".costPerBw"))
      .setCostPerMem(conf.getInt(SIM + "." + datacenter + ".costPerMem"))
      .setCostPerSecond(conf.getInt(SIM + "." + datacenter + ".cost"))
      .setCostPerStorage(conf.getInt(SIM + "." + datacenter + ".costPerStorage"))
    dc
  }

  /**
   * Creates a list of Hosts with its PES
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

        // here based on the service teh values are assigned or picked
        
        val numberOfCloudletService = (randomValue(numberOfCloudletsList, randomNumber))
        val numberOfCloudletDeafult = (randomValue(numberOfCloudletsList, 0))
        val numberOfCloudlet = if(serviceName != "FAAS" ) numberOfCloudletService else numberOfCloudletDeafult
        val CloudletPesService = (randomValue(cloudletPEsList, randomNumber))
        val CloudletPesDeafult = (randomValue(cloudletPEsList, 0))
        val CloudletPes = if(serviceName != "FAAS" ) CloudletPesService else CloudletPesDeafult
        val list = (1 to numberOfCloudlet).map(c => {
          val cloudlet = new CloudletSimple(cloudletLength, CloudletPes, utilizationModel)
          cloudlet.setSizes(1000)
          cloudlet
        }).toList
        list.asJava
      }

  //random function to pick a random number
  def randomNumber = {
    val r = scala.util.Random
    r.nextInt(3) //not inclusive
  }

  //object to Int conversion
  def objectToInt(s: Object) :Int = {
    s match {
      case n: java.lang.Integer => n
    }
  }

  //object to String conversion
  def objectToString(s: Object): String = {
    s match {
      case n: java.lang.String => n
    }
  }

  //based on the random number or fixed config the value is selected from the list of options
  def randomValue(randomData : ConfigList, randomOrNot : Int) : Int = {
    objectToInt(randomData.get(randomOrNot).unwrapped())
  }
  def randomValueStr(randomData : ConfigList, randomOrNot : Int) : String = {
    objectToString(operatingSystemList.get(randomNumber).unwrapped())
  }
  
  def main(args: Array[String]): Unit = {
        ServiceSimulation
      }
    }

