import org.rogach.scallop._
import org.json4s._
import org.json4s.jackson.JsonMethods
import com.eed3si9n.jarjarabrams.{ShadePattern, ShadeRule, Shader}
import java.util.zip.{ZipEntry, ZipOutputStream}
import os.{Generator, Path}
import java.io.{ByteArrayInputStream, InputStream}
import java.util.jar.JarFile
import scala.collection.JavaConverters._

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val jar = opt[String](required = true, descr = "The JAR to process")
  val out = opt[String](required = true, descr = "The output JAR path")
  val rules = opt[String](required = true, descr = "The JSON rules file containing jarjar rules")
  val verbose = opt[Boolean](required = false, default = Some(false), descr = "Run in verbose mode")
  validate(rules) { (r) =>
    if (os.exists(Path(r, os.pwd))) Right(())
    else Left("Rules file doesn't exist")
  }
  validate(jar) { (r) =>
    if (os.exists(Path(r, os.pwd))) Right(())
    else Left("Input jar file doesn't exist")
  }
  verify()
}

case class RenameRule(from: String, to: String)

case class JarJarRules(
    rename: Option[Seq[RenameRule]] = None,
    zap: Option[Seq[String]] = None,
    keep: Option[Seq[String]] = None,
)

object Main {
  type UnopenedInputStream = () => InputStream

  def parseRules(file: Path): Seq[ShadeRule] = {
    implicit val formats: Formats = DefaultFormats
    val json = JsonMethods.parse(os.read(file))

    val rules = json.extract[JarJarRules]

    List(
      rules.rename.map(rr => ShadePattern.Rename(rr.map(r => r.from -> r.to).toList).inAll),
      rules.zap.map(zr => ShadePattern.Zap(zr.toList).inAll),
      rules.keep.map(kr => ShadePattern.Keep(kr.toList).inAll),
    ).flatten
  }

  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)

    val shadeRules = parseRules(Path(conf.rules(), os.pwd))

    val shader =
      if (shadeRules.isEmpty) (name: String, inputStream: UnopenedInputStream) => Some(name -> inputStream)
      else {
        val shader = Shader.bytecodeShader(shadeRules, verbose = conf.verbose())
        (name: String, inputStream: UnopenedInputStream) =>
          shader(Streamable.bytes(inputStream()), name).map { case (bytes, name) =>
            name -> (() => new ByteArrayInputStream(bytes) { override def close(): Unit = inputStream().close() })
          }
      }

    val inputPath = Path(conf.jar(), os.pwd)
    val outputPath = Path(conf.out(), os.pwd)

    val outputStream = os.write.outputStream(outputPath)
    val zipOutputStream = new ZipOutputStream(outputStream)

    val jarFile = new JarFile(inputPath.toIO)
    Generator
      .from(jarFile.entries().asScala.filterNot(_.isDirectory))
      .flatMap(entry => {
        shader(entry.getName, () => jarFile.getInputStream(entry))
      })
      .foreach({ case (name, os) =>
        zipOutputStream.putNextEntry(new ZipEntry(name))
        Streamable.bytes(os()).foreach(b => zipOutputStream.write(b.toInt))
        zipOutputStream.closeEntry()
      })

    zipOutputStream.close()
    outputStream.close()

  }
}
