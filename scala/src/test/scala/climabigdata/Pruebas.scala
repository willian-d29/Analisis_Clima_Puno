package climabigdata

object Pruebas {
  private val datosPrueba: Seq[RegistroCrudo] =
    Seq(
      RegistroCrudo("2024-06-15 03:00:00", -15.5, -70.0, -7.5, 1.0, 2.0, 0.0),
      RegistroCrudo("2024-07-20 04:00:00", -14.9, -70.2, -12.0, 0.5, 0.5, 0.0),
      RegistroCrudo("2024-01-10 14:00:00", -16.2, -69.4, 6.0, 2.0, 1.0, 0.01),
      RegistroCrudo("2024-02-08 05:00:00", -15.8, -70.0, -1.0, 1.0, 1.0, 0.0)
    )

  def main(args: Array[String]): Unit = {
    val transformados = datosPrueba.map(Transformaciones.transformarRegistro)
    val heladas = transformados.filter(Filtros.esHelada)
    val invierno = transformados.filter(Filtros.esInvierno)
    val verano = transformados.filter(Filtros.esVerano)
    val resumen = Reducciones.procesarChunk(transformados)

    assert(heladas.length == 3)
    assert(invierno.length == 2)
    assert(verano.length == 2)
    assert(resumen.registros == 4)
    assert(resumen.totalHeladas == 3)
    assert(resumen.tempMinima.contains(-12.0))
    assert(resumen.invierno.heladas == 2)
    assert(resumen.verano.heladas == 1)
    assert(resumen.porProvincia.values.sum == 3)
    assert(resumen.estadisticasProvincia.values.map(_.totalHeladas).sum == 3)
    assert(resumen.eventosMasFrios.length == 3)
    assert(resumen.eventosMasFrios.head.temperatura == -12.0)

    println("Pruebas funcionales Scala OK")
  }
}
