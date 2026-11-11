# Reglas y Directrices del Proyecto Ajedrezzz

## 1. Convenciones de Control de Versiones (Git)
- **Commits por Fase:** Se debe realizar un commit en Git al finalizar e implementar satisfactoriamente cada fase del proyecto.
- **Idioma de los Commits:** Los mensajes de commit deben redactarse en **español**, describiendo claramente las funcionalidades añadidas, mejoras o refactorizaciones.
- **Ramas:** La rama principal de trabajo es `main`.

## 2. Documentación Local del Proyecto (`docs/`)
- La carpeta `docs/` se mantiene ignorada en Git (`.gitignore`) para notas, planes y seguimiento interno.
- **Estructura de prefijos obligatoria:**
  - `plan-fase-[X]-[nombre].md`: Plan detallado y requerimientos técnicos para cada fase antes o al inicio de su desarrollo.
  - `resul-fase-[X].md`: Resumen de resultados, estado de implementación, pruebas y pendientes de la fase.
  - `bug-list-[categoría].md`: Registro y seguimiento de errores/bugs con estado explícito: `[RESUELTO]`, `[PENDIENTE]` o `[EN PROCESO]`.

## 3. Arquitectura y Código
- **Patrón:** UDF (Unidirectional Data Flow) / MVI con ViewModel (`StateFlow<ChessUiState>`).
- **Compose Best Practices:** Evitar `LazyVerticalGrid` para el tablero 8x8; emplear `Column`/`Row` con ponderación de pesos o layouts custom de tamaño fijo.
- **Dominio Desacoplado:** La lógica externa del motor (`chesslib`) no debe invadir directamente los componentes UI de Compose; siempre se pasa a través de modelos de dominio inmutables (`Position`, `Piece`, `SquareUiModel`).
- **Enfoque Pedagógico:** Priorizar claridad visual para principiantes (diferenciación de movimientos pacíficos vs capturas, visión de jaque y detección de amenazas/piezas colgadas).
