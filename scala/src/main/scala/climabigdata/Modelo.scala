package climabigdata

final case class Fecha(anio: Int, mes: Int, dia: Int)

object Fecha {
  def aTexto(fecha: Fecha): String =
    f"${fecha.anio}%04d-${fecha.mes}%02d-${fecha.dia}%02d"
}

final case class RegistroCrudo(
    validTime: String,
    latitude: Double,
    longitude: Double,
    t2m: Double,
    u10: Double,
    v10: Double,
    tp: Double
)

final case class RegistroClimatico(
    fecha: Fecha,
    latitud: Double,
    longitud: Double,
    temperatura: Double,
    precipitacion: Double,
    velocidadViento: Double,
    riesgoHelada: String,
    provincia: String
)

final case class ConteoTemporada(registros: Long, heladas: Long)

object ConteoTemporada {
  val vacio: ConteoTemporada = ConteoTemporada(0L, 0L)
}

final case class EstadisticaProvincia(
    totalHeladas: Long,
    sumaTemperatura: Double,
    tempMinima: Option[Double]
)

object EstadisticaProvincia {
  val vacia: EstadisticaProvincia =
    EstadisticaProvincia(totalHeladas = 0L, sumaTemperatura = 0.0, tempMinima = None)
}

final case class EventoFrio(
    fecha: Fecha,
    provincia: String,
    latitud: Double,
    longitud: Double,
    temperatura: Double
)

final case class Resumen(
    registros: Long,
    totalHeladas: Long,
    sumaTempHeladas: Double,
    tempMinima: Option[Double],
    porProvincia: Map[String, Long],
    porAnio: Map[Int, Long],
    porMes: Map[Int, Long],
    porRiesgo: Map[String, Long],
    estadisticasProvincia: Map[String, EstadisticaProvincia],
    eventosMasFrios: Seq[EventoFrio],
    invierno: ConteoTemporada,
    verano: ConteoTemporada
)

object Resumen {
  val vacio: Resumen =
    Resumen(
      registros = 0L,
      totalHeladas = 0L,
      sumaTempHeladas = 0.0,
      tempMinima = None,
      porProvincia = Map.empty,
      porAnio = Map.empty,
      porMes = Map.empty,
      porRiesgo = Map.empty,
      estadisticasProvincia = Map.empty,
      eventosMasFrios = Seq.empty,
      invierno = ConteoTemporada.vacio,
      verano = ConteoTemporada.vacio
    )
}
