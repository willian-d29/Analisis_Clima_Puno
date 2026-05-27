package climabigdata

import java.io.File
import scala.io.Source
import scala.util.Try

object CargaDatos {
  def obtenerArchivosCsv(carpeta: String): Seq[File] = {
    val dir = new File(carpeta)
    Option(dir.listFiles)
      .getOrElse(Array.empty)
      .filter(archivo => archivo.isFile && archivo.getName.endsWith(".csv"))
      .sortBy(_.getName)
      .toSeq
  }

  def parsearLinea(linea: String): Option[RegistroCrudo] = {
    val columnas = linea.split(",", -1)
    if (columnas.length < 11) None
    else {
      Try(
        RegistroCrudo(
          validTime = columnas(0),
          latitude = columnas(1).toDouble,
          longitude = columnas(2).toDouble,
          t2m = columnas(3).toDouble,
          u10 = columnas(6).toDouble,
          v10 = columnas(7).toDouble,
          tp = columnas(10).toDouble
        )
      ).toOption
    }
  }

}
