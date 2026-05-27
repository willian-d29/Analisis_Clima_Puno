package climabigdata

object Reducciones {
  private val limiteEventosFrios = 10

  private def sumarConteo[K](mapa: Map[K, Long], clave: K, cantidad: Long = 1L): Map[K, Long] =
    mapa.updated(clave, mapa.getOrElse(clave, 0L) + cantidad)

  private def combinarConteos[K](a: Map[K, Long], b: Map[K, Long]): Map[K, Long] =
    b.foldLeft(a) { case (acc, (clave, valor)) => sumarConteo(acc, clave, valor) }

  private def combinarMinimo(a: Option[Double], b: Option[Double]): Option[Double] =
    (a, b) match {
      case (None, None) => None
      case (Some(x), None) => Some(x)
      case (None, Some(y)) => Some(y)
      case (Some(x), Some(y)) => Some(math.min(x, y))
    }

  private def actualizarEstadisticaProvincia(
      estadisticas: Map[String, EstadisticaProvincia],
      registro: RegistroClimatico
  ): Map[String, EstadisticaProvincia] = {
    val actual = estadisticas.getOrElse(registro.provincia, EstadisticaProvincia.vacia)
    val actualizado =
      EstadisticaProvincia(
        totalHeladas = actual.totalHeladas + 1,
        sumaTemperatura = actual.sumaTemperatura + registro.temperatura,
        tempMinima = combinarMinimo(actual.tempMinima, Some(registro.temperatura))
      )

    estadisticas.updated(registro.provincia, actualizado)
  }

  private def combinarEstadisticaProvincia(
      a: EstadisticaProvincia,
      b: EstadisticaProvincia
  ): EstadisticaProvincia =
    EstadisticaProvincia(
      totalHeladas = a.totalHeladas + b.totalHeladas,
      sumaTemperatura = a.sumaTemperatura + b.sumaTemperatura,
      tempMinima = combinarMinimo(a.tempMinima, b.tempMinima)
    )

  private def combinarEstadisticasProvincia(
      a: Map[String, EstadisticaProvincia],
      b: Map[String, EstadisticaProvincia]
  ): Map[String, EstadisticaProvincia] =
    b.foldLeft(a) { case (acc, (provincia, estadistica)) =>
      val combinada =
        combinarEstadisticaProvincia(
          acc.getOrElse(provincia, EstadisticaProvincia.vacia),
          estadistica
        )
      acc.updated(provincia, combinada)
    }

  private def eventoDesdeRegistro(registro: RegistroClimatico): EventoFrio =
    EventoFrio(
      fecha = registro.fecha,
      provincia = registro.provincia,
      latitud = registro.latitud,
      longitud = registro.longitud,
      temperatura = registro.temperatura
    )

  private def ordenarEventosFrios(eventos: Seq[EventoFrio]): Seq[EventoFrio] =
    eventos.sortBy(evento =>
      (
        evento.temperatura,
        evento.fecha.anio,
        evento.fecha.mes,
        evento.fecha.dia,
        evento.provincia,
        evento.latitud,
        evento.longitud
      )
    )

  private def actualizarEventosMasFrios(
      eventos: Seq[EventoFrio],
      registro: RegistroClimatico
  ): Seq[EventoFrio] =
    ordenarEventosFrios(eventos :+ eventoDesdeRegistro(registro)).take(limiteEventosFrios)

  private def combinarEventosMasFrios(a: Seq[EventoFrio], b: Seq[EventoFrio]): Seq[EventoFrio] =
    ordenarEventosFrios(a ++ b).take(limiteEventosFrios)

  private def registrarObservacion(resumen: Resumen, registro: RegistroClimatico): Resumen = {
    val invierno =
      if (Filtros.esInvierno(registro))
        resumen.invierno.copy(registros = resumen.invierno.registros + 1)
      else resumen.invierno

    val verano =
      if (Filtros.esVerano(registro))
        resumen.verano.copy(registros = resumen.verano.registros + 1)
      else resumen.verano

    resumen.copy(
      registros = resumen.registros + 1,
      porRiesgo = sumarConteo(resumen.porRiesgo, registro.riesgoHelada),
      invierno = invierno,
      verano = verano
    )
  }

  private def registrarHelada(resumen: Resumen, registro: RegistroClimatico): Resumen = {
    val invierno =
      if (Filtros.esInvierno(registro))
        resumen.invierno.copy(heladas = resumen.invierno.heladas + 1)
      else resumen.invierno

    val verano =
      if (Filtros.esVerano(registro))
        resumen.verano.copy(heladas = resumen.verano.heladas + 1)
      else resumen.verano

    resumen.copy(
      totalHeladas = resumen.totalHeladas + 1,
      sumaTempHeladas = resumen.sumaTempHeladas + registro.temperatura,
      tempMinima = combinarMinimo(resumen.tempMinima, Some(registro.temperatura)),
      porProvincia = sumarConteo(resumen.porProvincia, registro.provincia),
      porAnio = sumarConteo(resumen.porAnio, registro.fecha.anio),
      porMes = sumarConteo(resumen.porMes, registro.fecha.mes),
      estadisticasProvincia =
        actualizarEstadisticaProvincia(resumen.estadisticasProvincia, registro),
      eventosMasFrios = actualizarEventosMasFrios(resumen.eventosMasFrios, registro),
      invierno = invierno,
      verano = verano
    )
  }

  def procesarChunk(registros: Seq[RegistroClimatico]): Resumen = {
    val resumenObservaciones =
      registros.foldLeft(Resumen.vacio)(registrarObservacion)

    registros
      .filter(Filtros.esHelada)
      .foldLeft(resumenObservaciones)(registrarHelada)
  }

  def combinarResumen(a: Resumen, b: Resumen): Resumen =
    Resumen(
      registros = a.registros + b.registros,
      totalHeladas = a.totalHeladas + b.totalHeladas,
      sumaTempHeladas = a.sumaTempHeladas + b.sumaTempHeladas,
      tempMinima = combinarMinimo(a.tempMinima, b.tempMinima),
      porProvincia = combinarConteos(a.porProvincia, b.porProvincia),
      porAnio = combinarConteos(a.porAnio, b.porAnio),
      porMes = combinarConteos(a.porMes, b.porMes),
      porRiesgo = combinarConteos(a.porRiesgo, b.porRiesgo),
      estadisticasProvincia =
        combinarEstadisticasProvincia(a.estadisticasProvincia, b.estadisticasProvincia),
      eventosMasFrios = combinarEventosMasFrios(a.eventosMasFrios, b.eventosMasFrios),
      invierno = ConteoTemporada(
        registros = a.invierno.registros + b.invierno.registros,
        heladas = a.invierno.heladas + b.invierno.heladas
      ),
      verano = ConteoTemporada(
        registros = a.verano.registros + b.verano.registros,
        heladas = a.verano.heladas + b.verano.heladas
      )
    )
}
