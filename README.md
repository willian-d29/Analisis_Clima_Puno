# ClimaBigData

Proyecto academico para simular procesamiento Big Data climatico con principios
de programacion funcional, usando datos ERA5 del departamento de Puno, Peru
(2000-2025).

La implementacion principal ahora esta hecha en Scala para que el trabajo se
vea mas alineado con el curso de Lenguajes de Programacion y con ejercicios de
SML, Scala o Haskell. El objetivo no es construir un modelo de Machine
Learning. El foco es procesar un volumen grande de datos usando `map`,
`filter`, `foldLeft/reduce`, funciones de primera clase y procesamiento por
chunks.

## Dataset

Columnas principales:

- `valid_time`
- `latitude`
- `longitude`
- `t2m`
- `d2m`
- `sp`
- `u10`
- `v10`
- `number`
- `expver`
- `tp`

Total procesado esperado: 81,370,296 registros.

## Arquitectura Scala

- `scala/src/main/scala/climabigdata/Modelo.scala`: tipos del dominio con `case class`.
- `scala/src/main/scala/climabigdata/CargaDatos.scala`: descubrimiento y parseo de CSV.
- `scala/src/main/scala/climabigdata/Transformaciones.scala`: funciones tipo `map`.
- `scala/src/main/scala/climabigdata/Filtros.scala`: predicados para `filter`.
- `scala/src/main/scala/climabigdata/Reducciones.scala`: acumuladores y `foldLeft`.
- `scala/src/main/scala/climabigdata/Provincias.scala`: asignacion por coordenadas.
- `scala/src/main/scala/climabigdata/Procesamiento.scala`: pipeline por chunks.
- `scala/src/main/scala/climabigdata/Resultados.scala`: generacion de CSV finales.
- `scala/src/test/scala/climabigdata/Pruebas.scala`: casos de prueba.

La version principal del proyecto esta en `scala/`.

## Pipeline funcional

El procesamiento completo aplica la siguiente composicion:

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

La lectura por chunks permite procesar los 81M registros localmente sin cargar
el dataset completo en memoria. Cada chunk produce un resumen parcial y luego
todos los resumenes se combinan con una funcion asociativa.

## Estructura del repositorio

```text
ClimaBigData/
├── datos/
│   └── crudos/
│       ├── README.md
│       └── .gitkeep
├── informe/
│   └── informe_tecnico.md
├── scala/
│   ├── src/main/scala/climabigdata/
│   ├── src/test/scala/climabigdata/
│   ├── resultados/
│   ├── build.sbt
│   └── README.md
├── .gitignore
└── README.md
```

Los datasets ERA5 (`2000.csv` a `2025.csv`) no se suben a GitHub. Cada persona
debe colocarlos manualmente dentro de `datos/crudos/`.

## Criterios climaticos

- Helada: `t2m < 0`
- Riesgo BAJO: `t2m >= 0`
- Riesgo MEDIO: `-5 <= t2m < 0`
- Riesgo ALTO: `-10 <= t2m < -5`
- Riesgo EXTREMO: `t2m < -10`

La provincia se estima usando la provincia cuyo centro geografico aproximado
queda mas cerca de cada punto ERA5. Es una aproximacion academica reproducible,
sin depender de shapefiles ni GIS.

## Ejecucion

Instalar Scala CLI en macOS:

```bash
brew install scala-cli
```

Instalar Scala CLI en Windows con PowerShell:

```powershell
winget install VirtusLab.ScalaCLI
```

Verificar instalacion:

```bash
scala-cli --version
```

Ejecutar el proyecto:

```bash
cd scala
scala-cli --server=false --jvm 17 --scala 2.13.14 src/main/scala src/test/scala --main-class climabigdata.Pruebas
scala-cli --server=false --jvm 17 --scala 2.13.14 src/main/scala --main-class climabigdata.Main -- --datos=../datos/crudos --salida=resultados
```

## Resultados generados

- `scala/resultados/resumen_heladas.csv`
- `scala/resultados/heladas_por_anio.csv`
- `scala/resultados/heladas_por_mes.csv`
- `scala/resultados/heladas_por_provincia.csv`
- `scala/resultados/zonas_riesgo.csv`
- `scala/resultados/estadisticas_por_provincia.csv`
- `scala/resultados/ranking_riesgo_provincia.csv`
- `scala/resultados/eventos_mas_frios.csv`

La version Scala genera los CSV principales dentro de `scala/resultados/`.

## Estructura de datos

Los CSV crudos estan concentrados en una sola carpeta:

```text
datos/crudos/
├── 2000.csv
├── 2001.csv
...
└── 2025.csv
```

En GitHub esta carpeta queda vacia salvo por `README.md` y `.gitkeep`, para
mantener la estructura sin subir archivos pesados.

## Enfoque funcional

- `map`: transforma lineas CSV en registros climaticos enriquecidos.
- `filter`: selecciona heladas, invierno y verano.
- `foldLeft`: acumula observaciones, heladas y resumenes parciales.
- `case class`: representa datos inmutables del dominio climatico.
- funciones puras: las funciones principales reciben datos y devuelven nuevos
  datos o resumenes.
- acumuladores por grupo: se reduce por provincia para obtener conteo, suma,
  promedio y minimo.
- top funcional: se mantiene el top 10 de eventos mas frios con un acumulador
  acotado, sin guardar todos los eventos.

## Relacion con SML, Scala y Haskell

El trabajo parte de la misma idea usada en ejercicios de SML sobre listas:
procesar una coleccion separando el problema en funciones pequenas.

Ejemplo conceptual:

```sml
fun number_in_month (dates, month) =
    case dates of
        [] => 0
      | (_, m, _) :: rest =>
            (if m = month then 1 else 0) + number_in_month(rest, month)
```

En Scala, sobre millones de registros, esa recursividad directa no es
conveniente por memoria y limite de pila. Por eso se expresa el mismo enfoque
con funciones de orden superior:

```text
map(transformar_registro)
filter(es_helada)
foldLeft(combinar_resumen)
```

Equivalencias dentro del proyecto:

- `transformarRegistro`: equivale a aplicar `map`.
- `esHelada`, `esInvierno`, `esVerano`: equivalen a predicados usados por
  `filter`.
- `combinarResumen`: equivale a la funcion acumuladora de un `fold`.
- `procesarArchivo`: reduce todos los chunks de un CSV.
- `procesarTodosLosCsv`: reduce todos los resumenes anuales.

La diferencia principal es de escala: SML trabaja listas pequenas en memoria;
este proyecto trabaja 81M registros en chunks, pero conserva el mismo
razonamiento funcional.
