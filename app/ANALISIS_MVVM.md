# ANALISIS_MVVM — Taller 6
Programación para Dispositivos Móviles - Universidad de Caldas

## 1. Predicción inicial (escrita antes de programar)

**Pregunta:** si el `ViewModel` guarda la lista en una variable normal (`var cursosCache`) además del `MutableStateFlow`, y actualizo solo la variable pero no el `StateFlow`, ¿la UI refleja el cambio?

**Predicción:** **No.** La UI de Compose se suscribe al `StateFlow` (vía `collectAsStateWithLifecycle`), que convierte cada emisión en un `State` que dispara la recomposición. Una `var` común no emite nada: cambiar su valor no notifica a nadie, así que Compose nunca se entera.

**Resultado prediccion:** Al tocar la estrella, marcarFavorito se ejecutó (el log mostró cursosCache cambiando), pero ni la estrella ni el contador de favoritos cambiaron, porque _cursos nunca emitió un valor nuevo. Es decir, la prediccion fue correcta

## 2. bug de la Parte 2

**Qué hice:** implementé `marcarFavorito` actualizando solo `cursosCache` y no `_cursos.value`.

**Qué se observo:** la app no falla, el código se ejecuta, pero la estrella no cambia. La evidencia se encuentra en `capturas/bug.jpeg`

**Por qué ocurre:** Compose no vigila las propiedades de una clase. Solo recompone cuando cambia un `State` que se leyó durante la composición. `collectAsStateWithLifecycle()` observa el `StateFlow` `cursos`, y ese flujo solo emite cuando se asigna `_cursos.value` con un valor distinto. `cursosCache` es una variable arbitraria: modificarla no emite nada, aunque esté en la misma clase.

En el ejemplo del enunciado hay además un segundo problema: `cursosCache` arranca como `emptyList()` y nunca se llena con los datos cargados, así que el `.map` opera sobre una lista vacía y no cambia nada de todos modos. 

**Corrección:** eliminar `cursosCache` y actualizar directamente la fuente de verdad:

```kotlin
fun marcarFavorito(id: Int) {
    _cursos.value = _cursos.value.map {
        if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it
    }
}
```

Como `copy` crea un objeto nuevo y `map` una lista nueva, el `StateFlow` detecta un valor distinto y emite, la UI recompone. Verifiqué (Paso 2.4) que no queda ninguna `var` que duplique estado: el único estado mutable del ViewModel son `_cursos` y `_cargando`. `totalFavoritos` es derivado con `.map`, no un contador aparte.

## 3. Reflexión de la Parte 3: `remember` vs `ViewModel`

**Prueba:** marqué 2–3 favoritos, roté el dispositivo y siguieron marcados. La evidencia se encuentra en `capturas/estadoViewModel` Luego moví el estado a `remember { mutableStateOf(...) }` dentro del Composable, rotamos y los favoritos desaparecieron. 

**Qué hubiera pasado con `remember`:** al rotar, Android destruye y recrea la `Activity` (cambio de configuración). La composición se vuelve a construir desde cero, y `remember` solo conserva valores mientras el Composable permanece en la composición; al recrearse, la lista vuelve a su valor inicial y todos los `esFavorito` quedan en `false`. Además, la pantalla repetiría la carga con su `delay(1000)`, mostrando de nuevo el `CircularProgressIndicator`.

El `ViewModel`, en cambio, vive en el `ViewModelStore`, que se conserva durante la recreación de la `Activity`. La nueva composición vuelve a suscribirse a los mismos `StateFlow` y recibe el estado tal como estaba; `init` no se ejecuta otra vez porque es la misma instancia.

**Detalle de implementación:** el `ViewModel` se crea una sola vez en `MainActivity` y se pasa a lista y detalle. Si cada pantalla llamara `viewModel()` dentro de su `composable{}` del `NavHost`, cada una tendría su propia instancia (el alcance por defecto es la entrada del back stack) y marcar favorito en el detalle no se reflejaría en la lista.

## 4. Bonus realizado
- **Opción A:** `totalFavoritos`, un `StateFlow<Int>` derivado con `.map` + `stateIn` sobre `_cursos`, mostrado en la barra superior.