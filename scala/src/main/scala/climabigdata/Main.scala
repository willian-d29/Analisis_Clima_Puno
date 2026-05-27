package climabigdata

final case class Configuracion(
    carpetaDatos: String = "datos/crudos",
    carpetaSalida: String = "resultados",
    tamanoChunk: Int = 200000,
    limiteArchivos: Option[Int] = None
)

object Main {
  def parsearArgumentos(args: Array[String]): Configuracion =
    args.foldLeft(Configuracion()) { (config, arg) =>
      if (arg.startsWith("--datos=")) config.copy(carpetaDatos = arg.stripPrefix("--datos="))
      else if (arg.startsWith("--salida=")) config.copy(carpetaSalida = arg.stripPrefix("--salida="))
      else if (arg.startsWith("--chunk=")) config.copy(tamanoChunk = arg.stripPrefix("--chunk=").toInt)
      else if (arg.startsWith("--limite=")) config.copy(limiteArchivos = Some(arg.stripPrefix("--limite=").toInt))
      else config
    }

  def main(args: Array[String]): Unit = {
    val config = parsearArgumentos(args)

    println("\n=== PROCESAMIENTO FUNCIONAL BIG DATA EN SCALA ===")
    println(s"Datos: ${config.carpetaDatos}")
    println(s"Salida: ${config.carpetaSalida}")
    println(s"Tamano de chunk: ${config.tamanoChunk}")

    val resumen =
      Procesamiento.procesarTodosLosCsv(
        carpetaDatos = config.carpetaDatos,
        tamanoChunk = config.tamanoChunk,
        limiteArchivos = config.limiteArchivos
      )

    Resultados.guardarResumen(resumen, config.carpetaSalida)

    val promedio =
      if (resumen.totalHeladas == 0) 0.0
      else resumen.sumaTempHeladas / resumen.totalHeladas

    println("\n=== RESUMEN FINAL ===")
    println(s"Registros procesados: ${resumen.registros}")
    println(s"Total de heladas: ${resumen.totalHeladas}")
    println(s"Temperatura promedio en heladas: $promedio")
    println(s"Temperatura minima: ${resumen.tempMinima.getOrElse("sin heladas")}")
    println(s"Heladas en invierno: ${resumen.invierno.heladas}")
    println(s"Heladas en verano: ${resumen.verano.heladas}")

    println("\nArchivos generados:")
    println(s"${config.carpetaSalida}/resumen_heladas.csv")
    println(s"${config.carpetaSalida}/heladas_por_anio.csv")
    println(s"${config.carpetaSalida}/heladas_por_mes.csv")
    println(s"${config.carpetaSalida}/heladas_por_provincia.csv")
    println(s"${config.carpetaSalida}/zonas_riesgo.csv")
    println(s"${config.carpetaSalida}/estadisticas_por_provincia.csv")
    println(s"${config.carpetaSalida}/ranking_riesgo_provincia.csv")
    println(s"${config.carpetaSalida}/eventos_mas_frios.csv")
  }
}
