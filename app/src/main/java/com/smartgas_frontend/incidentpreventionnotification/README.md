# Incident Prevention & Notification

Bounded context de alertas y notificaciones preventivas (pestaña **Alerts** del diseño).

- `data/remote/AlertService` y `dto/` (AlertDto, NotificationDto)
- `data/repository/AlertRepository`
- `domain/model` (Alert, Notification)
- `presentation/alerts` (AlertsScreen + AlertsViewModel)

`postincidentprocedures` (reportes) y `shared` (Home) leen las alertas a través de `AlertService`.
