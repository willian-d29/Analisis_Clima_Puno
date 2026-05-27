package climabigdata

import java.io.File

object Procesamiento {
  def procesarArchivo(archivo: File, tamanoChunk: Int): Resumen = {
    val fuente = scala.io.Source.fromFile(archivo)

    try {
      val registros =
        fuente
          .getLines()
          .drop(1)
          .flatMap(CargaDatos.parsearLinea)
          .map(Transformaciones.transformarRegistro)

      registros
        .grouped(tamanoChunk)
        .map(Reducciones.procesarChunk)
        .foldLeft(Resumen.vacio)(Reducciones.combinarResumen)
    } finally {
      fuente.close()
    }
  }

  def procesarTodosLosCsv(
      carpetaDatos: String,
      tamanoChunk: Int,
      limiteArchivos: Option[Int]
  ): Resumen = {
    val archivos =
      limiteArchivos match {
        case Some(n) => CargaDatos.obtenerArchivosCsv(carpetaDatos).take(n)
        case None => CargaDatos.obtenerArchivosCsv(carpetaDatos)
      }

    archivos.zipWithIndex.foldLeft(Resumen.vacio) {
      case (resumenTotal, (archivo, indice)) =>
        println(s"Procesando archivo ${indice + 1}/${archivos.length}: ${archivo.getPath}")
        val resumenArchivo = procesarArchivo(archivo, tamanoChunk)
        Reducciones.combinarResumen(resumenTotal, resumenArchivo)
    }
  }
}
