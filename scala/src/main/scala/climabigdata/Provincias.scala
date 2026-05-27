package climabigdata

object Provincias {
  val provinciasPuno: Map[String, (Double, Double)] =
    Map(
      "Azangaro" -> (-14.91, -70.20),
      "Carabaya" -> (-14.07, -70.43),
      "Chucuito" -> (-16.21, -69.46),
      "El Collao" -> (-16.08, -69.64),
      "Huancane" -> (-15.20, -69.76),
      "Lampa" -> (-15.36, -70.37),
      "Melgar" -> (-14.88, -70.59),
      "Moho" -> (-15.36, -69.50),
      "Puno" -> (-15.84, -70.02),
      "San Antonio de Putina" -> (-14.91, -69.87),
      "San Roman" -> (-15.49, -70.13),
      "Sandia" -> (-14.32, -69.47),
      "Yunguyo" -> (-16.24, -69.09)
    )

  def distanciaCuadrada(a: (Double, Double), b: (Double, Double)): Double = {
    val dLat = a._1 - b._1
    val dLon = a._2 - b._2
    dLat * dLat + dLon * dLon
  }

  def asignarProvincia(latitud: Double, longitud: Double): String = {
    val punto = (latitud, longitud)
    provinciasPuno.minBy { case (_, centroide) => distanciaCuadrada(punto, centroide) }._1
  }
}
