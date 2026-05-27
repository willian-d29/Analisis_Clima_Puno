package climabigdata

object Filtros {
  def esHelada(registro: RegistroClimatico): Boolean =
    registro.temperatura < 0

  def esInvierno(registro: RegistroClimatico): Boolean =
    Set(6, 7, 8).contains(registro.fecha.mes)

  def esVerano(registro: RegistroClimatico): Boolean =
    Set(1, 2, 3).contains(registro.fecha.mes)
}
