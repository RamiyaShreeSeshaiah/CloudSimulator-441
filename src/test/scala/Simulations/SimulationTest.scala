package Simulations

//import Simulations.Simulation1.conf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import org.scalatest.funsuite.{AnyFunSuite}

class SimulationTest extends AnyFlatSpec with Matchers {

  val SIM = "simulation1config";
  val SIM1 = "simulation2config";
  
  val conf: Config = ConfigFactory.load(SIM + ".conf")
  val conf1: Config = ConfigFactory.load(SIM1 + ".conf")

  behavior of "configuration parameters module"

  it should "obtain the VM capacity" in {
    conf.getInt(SIM + ".vm.mips") shouldBe 5000
  }
  it should "obtain the VM Ram" in {
    conf.getInt(SIM + ".vm.ram") shouldBe 16000
  }
  it should "obtain the Host MIPS" in {
    conf.getInt(SIM + ".host.mips") shouldBe 15000
  }
  it should "obtain the Host Storage" in {
    conf.getInt(SIM + ".host.storage") shouldBe 1000000
  }
  it should "obtain the VM Bandwidth" in {
    conf.getInt(SIM + ".vm.bw") shouldBe 1000000
  }

  it should "obtain the VM capacity 1" in {
    conf1.getInt(SIM1 + ".vm.mips") shouldBe 5000
  }
  it should "obtain the VM Ram 1" in {
    conf1.getInt(SIM1 + ".vm.ram") shouldBe 512
  }

  it should "obtain the VM Bandwidth 1" in {
    conf1.getInt(SIM1 + ".vm.bw") shouldBe 1000
  }
}