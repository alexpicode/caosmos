# CI-07: Motor Fisiológico y Coste de Acciones

## Contexto

El ciudadano ya cuenta
con [BiologyManager](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/BiologyManager.java#9-84) (4
estados
enteros), [CitizenPulse](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/CitizenPulse.java#21-112)
como loop de
vida, [CitizenTaskManager](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/CitizenTaskManager.java#19-77) /
`TaskRegistry` para tareas largas
y [PerceptionMonitor](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/PerceptionMonitor.java#14-53)
para reflejos. La
interfaz [Task](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task/Task.java#9-21)
define [executeOnTick(Citizen, double dt, double walkingSpeed)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task/Task.java#11-20).
Esta tarea convierte ese esqueleto en un motor vivo con metabolismo pasivo, costes proporcionales al tiempo y umbrales
críticos con consecuencias reales.

---

## Proposed Changes

### 1. Dominio: `citizens/domain`

#### [MODIFY] [BiologyManager.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/BiologyManager.java)

- Cambiar campos `vitality`, `hunger`, `energy`, `stress` de `int` → `double`.
- Actualizar todos los métodos get/set/increase/decrease a `double`.
- Añadir método `applyRatePerHour(double ratePerHour, double deltaSeconds)` como helper interno para calcular
  variaciones proporcionales: `delta = ratePerHour * (deltaSeconds / 3600.0)`.

#### [MODIFY] [Status.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/perception/Status.java)

- Campos `int` → `double`.

#### [NEW] PhysiologicalThresholds.java _(citizens/domain)_

Clase `final` con constantes. Centraliza todos los valores del JIRA:

| Constante                               | Valor  |
|-----------------------------------------|--------|
| `HUNGER_CRISIS`                         | `80.0` |
| `ENERGY_EXTREME_FATIGUE`                | `15.0` |
| `ENERGY_COLLAPSE`                       | `5.0`  |
| `ENERGY_RECOVERY_WAKE`                  | `20.0` |
| `STRESS_PANIC`                          | `95.0` |
| `PASSIVE_HUNGER_RATE_PER_HOUR`          | `0.5`  |
| `PASSIVE_ENERGY_RATE_PER_HOUR`          | `-0.4` |
| `HUNGER_CRISIS_VITALITY_DRAIN_PER_HOUR` | `-1.0` |
| `EXTREME_FATIGUE_SPEED_FACTOR`          | `0.5`  |

#### [MODIFY] [Citizen.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/Citizen.java)

- Actualizar firmas
  de [consumeEnergy](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java#13-14), [decayVitality](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/Citizen.java#183-186), [eat](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/Citizen.java#222-226), [drink](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java#25-26), [sleep](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java#27-28)
  a `double`.
- Añadir: `applyStress(double)`,
  `reduceStress(double)`, [increaseHunger(double)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/BiologyManager.java#38-44),
  `applyPhysiologicalRates(double dtSeconds)` (delega
  a [BiologyManager](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/BiologyManager.java#9-84)).
- El campo `walkingSpeed` al navegar debe reducirse a `EXTREME_FATIGUE_SPEED_FACTOR` si
  `energy < ENERGY_EXTREME_FATIGUE`.

#### [NEW] Tasks en [citizens/domain/model/task](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task)

Implementan [Task](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task/Task.java#9-21)
con [executeOnTick(Citizen citizen, double dt, double walkingSpeed)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task/Task.java#11-20).
Los efectos se calculan con `applyRatePerHour(rate, dt)`.

| Clase                     | Tipo Acción | Efectos por Tick                                              | Fin de Tarea                                                      |
|---------------------------|-------------|---------------------------------------------------------------|-------------------------------------------------------------------|
| `SleepTask`               | `SLEEP`     | E +10/h, S -5/h, V +4/h (si H<80), H +0.2/h                   | `energy >= 100` o `energy >= ENERGY_RECOVERY_WAKE` si fue forzado |
| `WorkTask(workplaceType)` | `WORK`      | Mina: E -3.5, H +1.8, S +0.6 / Tienda: E -1.2, H +0.6, S +0.3 | Duración máxima o `energy < ENERGY_COLLAPSE`                      |
| `RestTask`                | `REST`      | E +2/h, S -1.5/h                                              | `energy >= 100` o interrupción                                    |
| `WaitTask(inSafeZone)`    | `WAIT`      | E -0.2/h, S -1.0/h (solo si inSafeZone)                       | 1 tick (se crea nueva cada decisión)                              |

> [!NOTE]
> [MoveToTargetTask](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task/MoveToTargetTask.java#11-59)
> ya existe. Se modificará para aplicar costes de navegación en cada tick: **E -1.5/h**, **H +1.0/h**. Si
`energy < ENERGY_EXTREME_FATIGUE`, el `walkingSpeed` se pasa multiplicado por `0.5`.

---

### 2. Contrato: `common/domain/contracts`

#### [MODIFY] [CitizenPort.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java)

- Actualizar firmas a
  `double`: [consumeEnergy](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java#13-14), [eat](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/Citizen.java#222-226), [drink](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java#25-26).
- Añadir:
    - `applyStress(UUID citizenId, double amount)`
    - `reduceStress(UUID citizenId, double amount)`
    - [increaseHunger(UUID citizenId, double amount)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/BiologyManager.java#38-44)
    - [getStatus(UUID citizenId)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/BiologyManager.java#80-83) → [Status](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/perception/Status.java#3-11)
    - `isInSafeZone(UUID citizenId)` → `boolean`
    - `assignPhysiologicalTask(UUID citizenId, Task task, String goalDescription)` → para que los handlers arranquen
      tareas continuas.

---

### 3. Motor Fisiológico: `citizens/application`

#### [NEW] PhysiologicalMotor.java _(citizens/application)_

`@Component`. Operaciones principales:

```
void applyPassiveMetabolism(Citizen citizen, double dtSeconds)
    → hambre +0.5/h, energía -0.4/h
    → si hunger > 80: vitality -1.0/h (el cuerpo se consume)

Optional<PhysiologicalReflex> evaluateCriticalThresholds(Citizen citizen)
    → energy < 5  → reflex crítico: FORCE_SLEEP
    → stress > 95 → reflex crítico: PANIC_FLEE
    → energy < 15 → evento informativo (no interrupta)
    → hunger > 80 → evento informativo
```

#### [NEW] PhysiologicalReflex.java _(citizens/application/model)_

```java
record PhysiologicalReflex(boolean critical, String reason, String forcedActionType, List<String> events)
```

#### [MODIFY] [CitizenPulse.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/CitizenPulse.java)

- Inyectar `PhysiologicalMotor`.
-

En [pulse(long tick)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/CitizenPulse.java#34-76),
reemplazar la llamada a `citizen.decayVitality(vitalityDecayAmount)` por:
1. `physiologicalMotor.applyPassiveMetabolism(citizen, pulseFrequencySeconds)`.
2. Evaluar `physiologicalMotor.evaluateCriticalThresholds(citizen)` y forzar
un [ReflexResult](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/perception/ReflexResult.java#5-12)
crítico si procede, que se pasa al flujo de interrupción ya existente
en [handleCriticalInterruption](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/CitizenPulse.java#77-97).

#### [MODIFY] [PulseConfiguration.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/model/PulseConfiguration.java)

- Eliminar `vitalityDecayAmount` (reemplazado por `PhysiologicalMotor`).
- Añadir `pulseFrequencySeconds` (valor de `CitizenSettings.pulseFrequency` convertido a seconds).

#### [MODIFY] [PerceptionMonitor.java](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/PerceptionMonitor.java)

-

Recibir [CitizenPerception](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/application/CitizenPerceptionHandler.java#19-55)
para acceder
a [Status](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/perception/Status.java#3-11).

- Añadir eventos informativos descriptivos al `informativeEvents`:
    - `vitality < 30` → "Tu cuerpo está gravemente herido."
    - `hunger > 80` → "Te mueres de hambre, debes comer ya."
    - `energy < 15` → "Estás al borde del colapso por agotamiento."
    - `stress > 80` → "Estás al límite de tu resistencia mental."

---

### 4. Handlers: `actions/application/handlers`

#### Acciones Instantáneas (sin cambio de arquitectura, solo firma `double`)

| Handler                                                                                                                                             | Costes Fisiológicos                                                                    |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| [EatActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/EatActionHandler.java#11-40)                 | `hunger -40.0`; si `highQuality=true` (param): `vitality +5.0`, `stress -5.0`          |
| `DrinkActionHandler`                                                                                                                                | `hunger -10.0`, `stress -2.0`, `vitality +2.0`                                         |
| [ExamineActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/ExamineActionHandler.java#12-38)         | `stress +0.5`, `energy -0.1`                                                           |
| [PickupActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/PickupActionHandler.java#13-46)           | `energy -0.5`                                                                          |
| `DropActionHandler`                                                                                                                                 | `energy -0.5`                                                                          |
| `EquipActionHandler`                                                                                                                                | `energy -0.3`                                                                          |
| `UnequipActionHandler`                                                                                                                              | `energy -0.3`                                                                          |
| [UseActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/UseActionHandler.java#12-38)                 | param `complexity`: `simple` → `energy -1.0`; `complex` → `energy -5.0`, `stress +0.5` |
| [CommunicateActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/CommunicateActionHandler.java#11-35) | `energy -1.0` (sin cambios funcionales)                                                |

#### [NEW] TalkActionHandler.java

- Acción `"TALK"`. Instantánea.
- Lee `targetId` de params; valida que exista en el mundo.
- Aplica `reduceStress(3.0)`.

#### Acciones Continuas — Handlers que registran una Task

| Handler                                                                                                                                       | Cambio                                                                           | Task registrada                                                                                                                                           |
|-----------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| [SleepActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/SleepActionHandler.java#11-28)       | `citizenService.assignPhysiologicalTask(id, new SleepTask(), "Dormiendo")`       | `SleepTask`                                                                                                                                               |
| `[NEW] WorkActionHandler`                                                                                                                     | Lee `workplaceType`, registra tarea con duración                                 | `WorkTask(workplaceType)`                                                                                                                                 |
| `[NEW] RestActionHandler`                                                                                                                     | Registra tarea                                                                   | `RestTask`                                                                                                                                                |
| [WaitActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/WaitActionHandler.java#11-28)         | Consulta `isInSafeZone`, registra tarea                                          | `WaitTask(inSafeZone)`                                                                                                                                    |
| [NavigateActionHandler](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/actions/application/handlers/NavigateActionHandler.java#14-99) | Sin cambios en el handler; costes se aplican en `MoveToTargetTask.executeOnTick` | [MoveToTargetTask](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/task/MoveToTargetTask.java#11-59) (existente, modificado) |

---

### 5. Infraestructura: [CitizenPort](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/common/domain/contracts/CitizenPort.java#7-31) →

`CitizenService`

#### [MODIFY] CitizenService.java _(citizens/infrastructure)_

- Implementar los nuevos métodos `double` y `isInSafeZone` (comprobando si la zona actual tiene el tag `[seguro]`).
- Implementar `assignPhysiologicalTask`.

---

## Verification Plan

### Automated Tests

```powershell
cd d:\Proyectos\caosmos
mvn test
```

-

Corregir [MoveToTargetTaskTest](file:///d:/Proyectos/caosmos/src/test/java/com/caosmos/citizens/domain/model/task/MoveToTargetTaskTest.java#17-64): [Status(100, 0, 100, 0)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/perception/Status.java#3-11) → [Status(100.0, 0.0, 100.0, 0.0)](file:///d:/Proyectos/caosmos/src/main/java/com/caosmos/citizens/domain/model/perception/Status.java#3-11).

- **[NEW]** `PhysiologicalMotorTest`: metabolismo base, crisis de hambre, umbrales de colapso/pánico.
- **[NEW]** `SleepTaskTest`, `WorkTaskTest`, `RestTaskTest`: efectos por tick y condiciones de finalización.

### Manual Verification

- Comprobar logs de biometría (`EntityTelemetryService`) en cada tick.
- Provocar `energy < 5` y verificar que el ciudadano fuerza `SLEEP`.
- Provocar `stress > 95` y verificar la huida hacia zona segura.
