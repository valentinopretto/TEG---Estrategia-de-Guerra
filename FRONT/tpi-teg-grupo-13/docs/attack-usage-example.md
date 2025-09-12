# Ejemplo de Uso del Feature de Ataque

## Escenario de Ejemplo

Imaginemos que estamos jugando TEG y queremos atacar desde **Brasil** hacia **Argentina**. El jugador tiene 5 ejércitos en Brasil y Argentina tiene 3 ejércitos defendidos por otro jugador.

## Paso a Paso

### 1. Verificar Condiciones Previas

```typescript
// Verificar que es nuestro turno
if (!this.isPlayerTurn()) {
  console.log('No es tu turno para atacar');
  return;
}

// Verificar que estamos en fase de ataque
if (!this.isAttackPhase()) {
  console.log('No estamos en fase de ataque');
  return;
}
```

### 2. Abrir Modal de Ataque

```typescript
// En GameScreenComponent
attack() {
  // Validaciones previas...
  this.isAttackModalOpen = true;
}
```

### 3. Seleccionar Territorio Atacante

En el modal, el jugador verá una lista de sus territorios:

```
🏰 Territorio Atacante
├── Brasil (5 ejércitos) ← Seleccionar
├── Perú (3 ejércitos)
├── Colombia (2 ejércitos)
└── México (4 ejércitos)
```

Al hacer clic en "Brasil", se actualiza la interfaz:

```typescript
// En AttackModalComponent
selectAttackerTerritory(territory) {
  this.selectedAttackerTerritory = territory; // Brasil
  this.loadAttackableTerritories(); // Carga territorios vecinos
  this.updateDiceLimits(); // Actualiza límites de dados
}
```

### 4. Seleccionar Territorio Defensor

Ahora aparecen los territorios atacables desde Brasil:

```
🛡️ Territorio Defensor
├── Argentina (3 ejércitos) ← Seleccionar
├── Uruguay (2 ejércitos)
└── Venezuela (1 ejército)
```

Al seleccionar "Argentina":

```typescript
selectDefenderTerritory(territory) {
  this.selectedDefenderTerritory = territory; // Argentina
  this.updateDiceLimits(); // Actualiza límites de dados
}
```

### 5. Configurar el Ataque

El jugador configura los parámetros del ataque:

```typescript
// Configuración automática basada en ejércitos
attackingArmies = 3; // Máximo: 4 (5-1)
attackerDice = 3;    // Máximo: 3 dados
defenderDice = 2;    // Máximo: 2 dados (defensor tiene 3 ejércitos)
```

### 6. Validar el Ataque

El sistema valida automáticamente:

```typescript
const validation = this.attackService.validateAttack(
  attackerTerritory, // Brasil
  defenderTerritory, // Argentina
  attackingArmies    // 3
);

// Resultado: { isValid: true }
```

### 7. Ejecutar el Ataque

```typescript
const attackRequest: AttackRequestDto = {
  gameCode: "ABC123",
  attackerTerritoryId: 15, // ID de Brasil
  defenderTerritoryId: 16, // ID de Argentina
  attackingArmies: 3,
  attackerDice: 3,
  defenderDice: 2
};

const response = await this.attackService.executeAttack(attackRequest);
```

### 8. Procesar el Resultado

El backend procesa el ataque y devuelve:

```json
{
  "success": true,
  "attackResult": {
    "attackerCountryId": "15",
    "attackerCountryName": "Brasil",
    "defenderCountryId": "16", 
    "defenderCountryName": "Argentina",
    "attackerPlayerName": "Jugador 1",
    "defenderPlayerName": "Jugador 2",
    "attackerDice": [6, 5, 4],
    "defenderDice": [3, 2],
    "attackerLosses": 1,
    "defenderLosses": 2,
    "territoryConquered": true,
    "attackerRemainingArmies": 2,
    "defenderRemainingArmies": 0
  },
  "gameState": {
    // Estado actualizado del juego
  }
}
```

### 9. Mostrar Notificación

```typescript
// El sistema muestra automáticamente
this.attackService.showAttackResult(response.attackResult);

// Resultado: "Jugador 1 conquistó Argentina desde Brasil"
```

### 10. Actualizar el Juego

```typescript
// Actualizar estado del juego
this.gamePlayService.updateGame(response.gameState);

// Cerrar modal
this.isAttackModalOpen = false;

// Mostrar notificación de éxito
this.notificationService.showNotification(
  'success', 
  'Ataque exitoso', 
  'Has conquistado Argentina!'
);
```

## Resultado Final

- **Brasil**: 2 ejércitos (5 - 3 atacantes + 0 perdidos)
- **Argentina**: 0 ejércitos, ahora pertenece a Jugador 1
- **Jugador 1**: Gana un territorio
- **Jugador 2**: Pierde un territorio

## Casos de Error

### Error 1: Territorios no vecinos

```typescript
// Si intentamos atacar desde Brasil a México
const validation = this.attackService.validateAttack(
  brasil,    // canAttack: true
  mexico,    // canBeAttacked: false (no es vecino)
  3
);

// Resultado: { isValid: false, message: "No puedes atacar este territorio" }
```

### Error 2: Mismo propietario

```typescript
// Si intentamos atacar desde Brasil a Perú (ambos del mismo jugador)
const validation = this.attackService.validateAttack(
  brasil,    // ownerName: "Jugador 1"
  peru,      // ownerName: "Jugador 1"
  3
);

// Resultado: { isValid: false, message: "No puedes atacar tu propio territorio" }
```

### Error 3: Ejércitos insuficientes

```typescript
// Si intentamos atacar con todos los ejércitos
const validation = this.attackService.validateAttack(
  brasil,    // armies: 5
  argentina, // armies: 3
  5          // attackingArmies: 5
);

// Resultado: { isValid: false, message: "Debes dejar al menos 1 ejército en el territorio atacante" }
```

## Interfaz Visual

### Estado Inicial
```
┌─────────────────────────────────────┐
│ ⚔️ Ataque                          │
├─────────────────────────────────────┤
│ 🏰 Territorio Atacante              │
│ [Brasil (5)] [Perú (3)] [Colombia]  │
│                                     │
│ 🛡️ Territorio Defensor              │
│ Selecciona primero un atacante      │
└─────────────────────────────────────┘
```

### Después de Seleccionar Atacante
```
┌─────────────────────────────────────┐
│ ⚔️ Ataque                          │
├─────────────────────────────────────┤
│ 🏰 Territorio Atacante              │
│ ✅ Brasil (5 ejércitos) [Cambiar]   │
│                                     │
│ 🛡️ Territorio Defensor              │
│ [Argentina (3)] [Uruguay (2)]       │
└─────────────────────────────────────┘
```

### Configuración Completa
```
┌─────────────────────────────────────┐
│ ⚔️ Ataque                          │
├─────────────────────────────────────┤
│ 🏰 Brasil (5) → 🛡️ Argentina (3)   │
│                                     │
│ ⚔️ Configuración del Ataque         │
│ Ejércitos: [3] (máx: 4)             │
│ Dados atacante: [3] (máx: 3)        │
│ Dados defensor: [2] (máx: 2)        │
│                                     │
│ 📋 Resumen del Ataque               │
│ Desde: Brasil                       │
│ Hacia: Argentina                    │
│ Ejércitos: 3                        │
│                                     │
│ [Cancelar] [⚔️ Atacar]              │
└─────────────────────────────────────┘
```

## Consejos de Uso

1. **Planifica tus ataques**: Considera la posición estratégica
2. **Usa suficientes ejércitos**: No ataques con solo 1 ejército
3. **Considera los dados**: Más dados = mayor probabilidad de victoria
4. **Protege tus territorios**: Deja ejércitos para defender
5. **Observa el mapa**: Identifica territorios vulnerables

## Próximos Pasos

Después de un ataque exitoso, puedes:

1. **Continuar atacando**: Realizar más ataques desde el mismo territorio
2. **Mover ejércitos**: Reforzar el territorio conquistado
3. **Terminar turno**: Pasar al siguiente jugador
4. **Intercambiar cartas**: Si tienes 3 cartas del mismo tipo 