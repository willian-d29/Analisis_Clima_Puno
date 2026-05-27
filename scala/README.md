# ClimaBigData Scala

Version 100% Scala del procesamiento funcional de datos climaticos ERA5 de Puno.

El objetivo es mostrar con mas claridad los conceptos vistos en Lenguajes de
Programacion:

- funciones de primera clase
- `map`
- `filter`
- `foldLeft` / `reduce`
- datos inmutables con `case class`
- procesamiento por chunks para simular Big Data

## Ejecutar con scala-cli

Instalacion en macOS:

```bash
brew install scala-cli
```

Desde la carpeta `scala/`:

```bash
scala-cli --server=false --jvm 17 --scala 2.13.14 src/main/scala src/test/scala --main-class climabigdata.Pruebas
scala-cli --server=false --jvm 17 --scala 2.13.14 src/main/scala --main-class climabigdata.Main -- --datos=../datos/crudos --salida=resultados
```

Para una prueba rapida con un solo CSV:

```bash
scala-cli --server=false --jvm 17 --scala 2.13.14 src/main/scala --main-class climabigdata.Main -- --datos=../datos/crudos --salida=resultados_prueba --limite=1
```

Tambien se incluye `build.sbt` para usar `sbt` si esta instalado:

```bash
sbt "runMain climabigdata.Pruebas"
sbt "runMain climabigdata.Main --datos=../datos/crudos --salida=resultados"
```

Los resultados finales quedan dentro de esta misma carpeta:

```text
scala/resultados/
├── resumen_heladas.csv
├── heladas_por_anio.csv
├── heladas_por_mes.csv
├── heladas_por_provincia.csv
├── zonas_riesgo.csv
├── estadisticas_por_provincia.csv
├── ranking_riesgo_provincia.csv
└── eventos_mas_frios.csv
```

## Pipeline funcional

```text
CSV -> Iterator[String]
    -> flatMap(parsearLinea)
    -> map(transformarRegistro)
    -> grouped(chunk)
    -> map(procesarChunk)
    -> foldLeft(combinarResumen)
    -> CSV de resultados
```

Dentro de cada chunk:

```text
registros.foldLeft(registrarObservacion)
registros.filter(esHelada).foldLeft(registrarHelada)
```

Este diseno conserva la idea de las funciones recursivas sobre listas de SML,
pero evita cargar los 81M registros completos en memoria.

## Analisis adicionales

- `estadisticas_por_provincia.csv`: total de heladas, promedio de temperatura
  durante heladas y temperatura minima por provincia.
- `ranking_riesgo_provincia.csv`: provincias ordenadas por cantidad de heladas
  y clasificadas en riesgo BAJO/MEDIO/ALTO/EXTREMO.
- `eventos_mas_frios.csv`: top 10 de eventos de helada mas severos.
