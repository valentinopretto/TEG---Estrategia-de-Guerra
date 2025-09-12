# Feature de Ataque - TEG Game

## Descripción

Este documento describe la implementación del feature de ataque para el juego TEG (Tactical European Game). El sistema permite a los jugadores atacar territorios enemigos siguiendo las reglas clásicas del juego.

## Componentes Implementados

### 1. AttackService (`src/app/features/game/services/attack.service.ts`)

Servicio principal que maneja toda la lógica de ataques:

- **executeAttack()**: Ejecuta un ataque entre territorios
- **validateAttack()**: Valida si un ataque es legal
- **calculateMaxAttackerDice()**: Calcula el máximo de dados del atacante
- **calculateMaxDefenderDice()**: Calcula el máximo de dados del defensor
- **showAttackResult()**: Muestra notificaciones de resultado

### 2. AttackModalComponent (`src/app/features/game/components/attack-modal/`)

Modal interactivo para configurar y ejecutar ataques:

- **Selección de territorios**: Atacante y defensor
- **Configuración de ejércitos**: Número de ejércitos para atacar
- **Configuración de dados**: Dados del atacante y defensor
- **Validación en tiempo real**: Verifica reglas del juego
- **Interfaz intuitiva**: Diseño responsive y accesible

### 3. TerritoryInfoComponent (`src/app/features/game/components/territory-info/`)

Panel de información detallada de territorios:

- **Información del territorio**: Propietario, ejércitos, continente
- **Estado visual**: Indica si puede atacar o ser atacado
- **Acciones rápidas**: Botones para seleccionar territorio
- **Diseño compacto**: Se muestra al hacer clic en territorios

## Reglas de Ataque Implementadas

### Validaciones Básicas

1. **Turno del jugador**: Solo se puede atacar en el turno propio
2. **Fase del juego**: Solo se puede atacar en fases apropiadas
3. **Propiedad del territorio**: Solo se puede atacar desde territorios propios
4. **Territorio enemigo**: No se puede atacar territorios propios
5. **Ejércitos mínimos**: Debe quedar al menos 1 ejército en el territorio atacante
6. **Territorios vecinos**: Solo se puede atacar territorios adyacentes

### Mecánica de Dados

- **Atacante**: Máximo 3 dados (o menos si tiene menos ejércitos)
- **Defensor**: Máximo 2 dados (o menos si tiene menos ejércitos)
- **Comparación**: Se comparan los dados más altos
- **Pérdidas**: El perdedor pierde 1 ejército por cada dado perdido

### Conquista de Territorios

- Si el defensor pierde todos sus ejércitos, el territorio es conquistado
- El atacante debe mover al menos 1 ejército al territorio conquistado
- Se actualiza la propiedad del territorio

## Flujo de Uso

### 1. Iniciar Ataque

```typescript
// En GameScreenComponent
attack() {
  if (!this.isPlayerTurn()) {
    this.notificationService.showNotification('warning', 'No es tu turno');
    return;
  }
  
  if (!this.isAttackPhase()) {
    this.notificationService.showNotification('warning', 'Fase incorrecta');
    return;
  }
  
  this.isAttackModalOpen = true;
}
```

### 2. Seleccionar Territorios

```typescript
// En AttackModalComponent
selectAttackerTerritory(territory: CountryResponseDto): void {
  this.selectedAttackerTerritory = territory;
  this.loadAttackableTerritories();
  this.updateDiceLimits();
}
```

### 3. Configurar Ataque

```typescript
// Configurar ejércitos y dados
const attackRequest: AttackRequestDto = {
  gameCode: this.gameCode,
  attackerTerritoryId: this.selectedAttackerTerritory!.id,
  defenderTerritoryId: this.selectedDefenderTerritory!.id,
  attackingArmies: this.attackingArmies,
  attackerDice: this.attackerDice,
  defenderDice: this.defenderDice
};
```

### 4. Ejecutar Ataque

```typescript
// Enviar al backend
const response = await firstValueFrom(this.attackService.executeAttack(attackRequest));

if (response.success) {
  this.attackService.showAttackResult(response.attackResult);
  this.attackExecuted.emit(response.gameState);
}
```

## Interfaces de Datos

### AttackRequestDto

```typescript
export interface AttackRequestDto {
  gameCode: string;
  attackerTerritoryId: number;
  defenderTerritoryId: number;
  attackingArmies: number;
  attackerDice: number;
  defenderDice: number;
}
```

### AttackResponseDto

```typescript
export interface AttackResponseDto {
  success: boolean;
  attackResult: AttackResult;
  gameState: any; // GameResponseDto actualizado
  message?: string;
}
```

### AttackResult

```typescript
export interface AttackResult {
  attackerCountryId: string;
  attackerCountryName: string;
  defenderCountryId: string;
  defenderCountryName: string;
  attackerPlayerName: string;
  defenderPlayerName: string;
  attackerDice: number[];
  defenderDice: number[];
  attackerLosses: number;
  defenderLosses: number;
  territoryConquered: boolean;
  attackerRemainingArmies: number;
  defenderRemainingArmies: number;
}
```

## Endpoints del Backend

### Ejecutar Ataque

```
POST /api/games/{gameCode}/attack
```

**Body:**
```json
{
  "attackerTerritoryId": 1,
  "defenderTerritoryId": 2,
  "attackingArmies": 3,
  "attackerDice": 3,
  "defenderDice": 2
}
```

### Obtener Territorios Atacables

```
GET /api/games/{gameCode}/territories/{territoryId}/attackable
```

### Obtener Territorios Atacantes

```
GET /api/games/{gameCode}/territories/{territoryId}/attackers
```

## Notificaciones

El sistema incluye notificaciones para:

- **Resultado de ataque**: Victoria o derrota
- **Territorio conquistado**: Confirmación de conquista
- **Errores de validación**: Reglas violadas
- **Errores de red**: Problemas de comunicación

## Estilos y UI

### Diseño Responsive

- **Desktop**: Modal de 2 columnas con información detallada
- **Mobile**: Modal de 1 columna optimizado para touch
- **Tablet**: Diseño híbrido adaptable

### Estados Visuales

- **Territorio atacante**: Azul con icono de espada
- **Territorio defensor**: Rojo con icono de escudo
- **Territorio neutral**: Gris sin acciones disponibles
- **Carga**: Spinner animado durante la ejecución

### Animaciones

- **Entrada del modal**: Slide desde la derecha
- **Botones**: Efectos hover y active
- **Contadores**: Animación pulse al actualizar
- **Transiciones**: Suaves entre estados

## Próximas Mejoras

1. **Ataque múltiple**: Permitir varios ataques consecutivos
2. **Animaciones de batalla**: Efectos visuales durante el combate
3. **Historial de ataques**: Lista de ataques recientes
4. **Estadísticas**: Contador de victorias/derrotas
5. **Sonidos**: Efectos de audio para ataques
6. **Tutorial**: Guía interactiva para nuevos jugadores

## Testing

### Casos de Prueba

1. **Ataque válido**: Territorios vecinos, ejércitos suficientes
2. **Ataque inválido**: Mismo propietario, territorios no vecinos
3. **Conquista exitosa**: Defensor pierde todos los ejércitos
4. **Ataque fallido**: Atacante pierde más ejércitos
5. **Validaciones de dados**: Límites correctos según ejércitos

### Métricas

- **Tiempo de respuesta**: < 2 segundos para ataques
- **Usabilidad**: < 3 clics para completar un ataque
- **Accesibilidad**: Compatible con lectores de pantalla
- **Performance**: Sin lag en dispositivos móviles

## Dependencias

- **Angular**: Framework principal
- **RxJS**: Manejo de observables
- **SweetAlert2**: Notificaciones avanzadas
- **Tailwind CSS**: Estilos y responsive design

## Instalación y Configuración

1. **Importar componentes** en el módulo correspondiente
2. **Configurar rutas** del backend para los endpoints
3. **Ajustar estilos** según el tema del juego
4. **Probar funcionalidad** con diferentes escenarios
5. **Optimizar performance** según necesidades

## Soporte

Para problemas o mejoras:

1. Revisar logs del navegador
2. Verificar conectividad con el backend
3. Validar datos del juego
4. Consultar documentación de Angular
5. Contactar al equipo de desarrollo 