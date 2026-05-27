# Informe tecnico - ClimaBigData Scala

## Objetivo

Procesar datos climaticos ERA5 de Puno usando Scala, simulando un flujo Big
Data inspirado en Spark/Hadoop y basado en programacion funcional.

## Paradigma funcional aplicado

El proyecto separa el procesamiento en funciones pequenas:

- Transformaciones: convierten registros crudos en registros enriquecidos.
- Predicados: expresan condiciones como `es_helada`, `es_invierno` y
  `es_verano`.
- Reducciones: acumulan conteos, minimos y sumas a partir de resumenes
  parciales.

Esta division permite explicar el flujo como:

```text
map -> filter -> reduce
```

La relacion con SML es directa: las funciones recursivas sobre listas se
reemplazan por funciones de orden superior de Python. Por ejemplo, un conteo
recursivo como `number_in_month` en SML se representa aqui como un filtro por
mes y una reduccion/acumulacion. La idea es la misma: dividir la lista en
elementos, aplicar un predicado y combinar resultados parciales.

En el codigo Scala:

- `map` aparece al transformar lineas CSV en registros climaticos.
- `filter` aparece en los predicados de helada, invierno y verano.
- `foldLeft` aparece al combinar registros, chunks y archivos completos.
- `combinarResumen` es la funcion acumuladora equivalente a un fold funcional.

## Procesamiento por chunks

El dataset completo no se carga en memoria. Los CSV se encuentran en
`datos/crudos/` con el nombre del anio (`2000.csv`, `2001.csv`, ...,
`2025.csv`). Cada CSV se lee como `Iterator`, se agrupa con `grouped(chunk)` y
cada chunk genera un resumen parcial. Luego, los resumenes se combinan con
`combinarResumen`, una funcion de tipo fold.

## Estadisticas generadas

- total de registros procesados
- total de heladas
- temperatura promedio durante heladas
- temperatura minima
- heladas por provincia
- heladas por anio
- heladas por mes
- registros por nivel de riesgo
- comparacion de casos invierno/verano
- estadisticas termicas por provincia
- ranking de riesgo provincial
- top 10 de eventos mas frios

Los CSV finales se guardan dentro de `scala/resultados/`.

## Asignacion provincial

Cada punto de la grilla ERA5 se asigna a una provincia de Puno mediante la
distancia al centro geografico aproximado de cada provincia. Este metodo es
ligero y suficiente para una simulacion academica. Para una investigacion GIS
formal, el siguiente paso seria usar poligonos administrativos oficiales.

## Archivos principales

- `scala/src/main/scala/climabigdata/Main.scala`: punto de entrada.
- `Procesamiento.scala`: orquesta el pipeline funcional por chunks.
- `Transformaciones.scala`: contiene funciones tipo map.
- `Filtros.scala`: contiene predicados y filtros.
- `Reducciones.scala`: contiene fold/reduce de resumenes.
- `Resultados.scala`: escribe CSV.
- `Pruebas.scala`: contiene los dos casos de prueba principales.
