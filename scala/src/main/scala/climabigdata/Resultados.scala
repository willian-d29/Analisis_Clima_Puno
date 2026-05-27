package climabigdata

import java.io.{File, PrintWriter}

object Resultados {
  private val ordenRiesgo = Seq("BAJO", "MEDIO", "ALTO", "EXTREMO")

  private def escribir(ruta: File)(contenido: PrintWriter => Unit): Unit = {
    ruta.getParentFile.mkdirs()
    val writer = new PrintWriter(ruta)
    try contenido(writer)
    finally writer.close()
  }

  private def escribirConteos[K](
      ruta: File,
      cabecera: String,
      conteos: Seq[(K, Long)]
  ): Unit =
    escribir(ruta) { writer =>
      writer.println(cabecera)
      conteos.foreach { case (clave, valor) => writer.println(s"$clave,$valor") }
    }

  private def clasificarRiesgoProvincia(totalHeladas: Long): String =
    if (totalHeladas >= 750000) "EXTREMO"
    else if (totalHeladas >= 300000) "ALTO"
    else if (totalHeladas >= 100000) "MEDIO"
    else "BAJO"

  def guardarResumen(resumen: Resumen, carpetaSalida: String): Unit = {
    val salida = new File(carpetaSalida)
    salida.mkdirs()

    val promedio =
      if (resumen.totalHeladas == 0) 0.0
      else resumen.sumaTempHeladas / resumen.totalHeladas

    escribir(new File(salida, "resumen_heladas.csv")) { writer =>
      writer.println(
        "registros_procesados,total_heladas,temperatura_promedio_heladas,temperatura_minima,heladas_invierno,heladas_verano"
      )
      writer.println(
        s"${resumen.registros},${resumen.totalHeladas},$promedio,${resumen.tempMinima.getOrElse("")},${resumen.invierno.heladas},${resumen.verano.heladas}"
      )
    }

    escribirConteos(
      new File(salida, "heladas_por_anio.csv"),
      "anio,total_heladas",
      resumen.porAnio.toSeq.sortBy(_._1)
    )

    escribirConteos(
      new File(salida, "heladas_por_mes.csv"),
      "mes,total_heladas",
      resumen.porMes.toSeq.sortBy(_._1)
    )

    escribirConteos(
      new File(salida, "heladas_por_provincia.csv"),
      "provincia,total_heladas",
      resumen.porProvincia.toSeq.sortBy(_._1)
    )

    val riesgoOrdenado =
      ordenRiesgo.map(riesgo => riesgo -> resumen.porRiesgo.getOrElse(riesgo, 0L))

    escribirConteos(
      new File(salida, "zonas_riesgo.csv"),
      "riesgo,registros",
      riesgoOrdenado
    )

    escribir(new File(salida, "estadisticas_por_provincia.csv")) { writer =>
      writer.println("provincia,total_heladas,temperatura_promedio_heladas,temperatura_minima")
      resumen.estadisticasProvincia.toSeq.sortBy(_._1).foreach {
        case (provincia, estadistica) =>
          val promedio =
            if (estadistica.totalHeladas == 0) 0.0
            else estadistica.sumaTemperatura / estadistica.totalHeladas
          writer.println(
            s"$provincia,${estadistica.totalHeladas},$promedio,${estadistica.tempMinima.getOrElse("")}"
          )
      }
    }

    escribir(new File(salida, "ranking_riesgo_provincia.csv")) { writer =>
      writer.println("provincia,total_heladas,nivel_riesgo")
      resumen.porProvincia.toSeq
        .sortBy { case (provincia, total) => (-total, provincia) }
        .foreach { case (provincia, total) =>
          writer.println(s"$provincia,$total,${clasificarRiesgoProvincia(total)}")
        }
    }

    escribir(new File(salida, "eventos_mas_frios.csv")) { writer =>
      writer.println("fecha,provincia,latitud,longitud,temperatura")
      resumen.eventosMasFrios.foreach { evento =>
        writer.println(
          s"${Fecha.aTexto(evento.fecha)},${evento.provincia},${evento.latitud},${evento.longitud},${evento.temperatura}"
        )
      }
    }
  }
}
