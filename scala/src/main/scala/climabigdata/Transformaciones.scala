package climabigdata

object Transformaciones {
  def clasificarRiesgo(temperatura: Double): String =
    if (temperatura < -10) "EXTREMO"
    else if (temperatura < -5) "ALTO"
    else if (temperatura < 0) "MEDIO"
    else "BAJO"

  def parsearFecha(validTime: String): Fecha = {
    val anio = validTime.substring(0, 4).toInt
    val mes = validTime.substring(5, 7).toInt
    val dia = validTime.substring(8, 10).toInt
    Fecha(anio, mes, dia)
  }

  def transformarRegistro(crudo: RegistroCrudo): RegistroClimatico = {
    val fecha = parsearFecha(crudo.validTime)
    val velocidadViento = math.sqrt(crudo.u10 * crudo.u10 + crudo.v10 * crudo.v10)
    val provincia = Provincias.asignarProvincia(crudo.latitude, crudo.longitude)

    RegistroClimatico(
      fecha = fecha,
      latitud = crudo.latitude,
      longitud = crudo.longitude,
      temperatura = crudo.t2m,
      precipitacion = crudo.tp,
      velocidadViento = velocidadViento,
      riesgoHelada = clasificarRiesgo(crudo.t2m),
      provincia = provincia
    )
  }
}
