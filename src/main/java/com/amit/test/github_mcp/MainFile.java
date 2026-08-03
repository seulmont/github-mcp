import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MainFile
 *
 * A self-contained demonstration of MineStar-style fleet management
 * business logic: equipment tracking, crew assignment, site/zone
 * management, fuel and utilization monitoring, and maintenance
 * scheduling. This is illustrative sample code (not tied to any
 * specific proprietary module) intended to show patterns typically
 * used in a mine fleet management system.
 */
public class MainFile {

    // ------------------------------------------------------------------
    // Domain enums
    // ------------------------------------------------------------------

    public enum EquipmentType {
        HAUL_TRUCK, EXCAVATOR, DOZER, DRILL, LOADER, GRADER, WATER_CART
    }

    public enum EquipmentStatus {
        IDLE, OPERATING, DELAYED, DOWN, MAINTENANCE, OFFLINE
    }

    public enum CrewRole {
        OPERATOR, SUPERVISOR, MAINTENANCE_TECH, DISPATCHER
    }

    public enum AlertSeverity {
        INFO, WARNING, CRITICAL
    }

    // ------------------------------------------------------------------
    // Domain model: Equipment
    // ------------------------------------------------------------------

    public static class Equipment {
        private final String equipmentId;
        private final EquipmentType type;
        private EquipmentStatus status;
        private double fuelLevelPercent;
        private double odometerHours;
        private String currentZoneId;
        private String assignedCrewId;
        private Instant lastStatusChange;
        private final List<MaintenanceRecord> maintenanceHistory = new ArrayList<>();

        public Equipment(String equipmentId, EquipmentType type) {
            this.equipmentId = Objects.requireNonNull(equipmentId);
            this.type = Objects.requireNonNull(type);
            this.status = EquipmentStatus.IDLE;
            this.fuelLevelPercent = 100.0;
            this.odometerHours = 0.0;
            this.lastStatusChange = Instant.now();
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public EquipmentType getType() {
            return type;
        }

        public EquipmentStatus getStatus() {
            return status;
        }

        public void setStatus(EquipmentStatus status) {
            this.status = status;
            this.lastStatusChange = Instant.now();
        }

        public double getFuelLevelPercent() {
            return fuelLevelPercent;
        }

        public void consumeFuel(double percent) {
            this.fuelLevelPercent = Math.max(0.0, this.fuelLevelPercent - percent);
        }

        public void refuel(double percent) {
            this.fuelLevelPercent = Math.min(100.0, this.fuelLevelPercent + percent);
        }

        public double getOdometerHours() {
            return odometerHours;
        }

        public void addOperatingHours(double hours) {
            this.odometerHours += hours;
        }

        public String getCurrentZoneId() {
            return currentZoneId;
        }

        public void setCurrentZoneId(String currentZoneId) {
            this.currentZoneId = currentZoneId;
        }

        public String getAssignedCrewId() {
            return assignedCrewId;
        }

        public void setAssignedCrewId(String assignedCrewId) {
            this.assignedCrewId = assignedCrewId;
        }

        public Instant getLastStatusChange() {
            return lastStatusChange;
        }

        public List<MaintenanceRecord> getMaintenanceHistory() {
            return maintenanceHistory;
        }

        public void addMaintenanceRecord(MaintenanceRecord record) {
            this.maintenanceHistory.add(record);
        }

        @Override
        public String toString() {
            return String.format("Equipment[%s, type=%s, status=%s, fuel=%.1f%%, hours=%.1f]",
                    equipmentId, type, status, fuelLevelPercent, odometerHours);
        }
    }

    // ------------------------------------------------------------------
    // Domain model: MaintenanceRecord
    // ------------------------------------------------------------------

    public static class MaintenanceRecord {
        private final String recordId;
        private final String description;
        private final Instant performedAt;
        private final double hoursAtService;

        public MaintenanceRecord(String description, double hoursAtService) {
            this.recordId = UUID.randomUUID().toString();
            this.description = description;
            this.performedAt = Instant.now();
            this.hoursAtService = hoursAtService;
        }

        public String getRecordId() {
            return recordId;
        }

        public String getDescription() {
            return description;
        }

        public Instant getPerformedAt() {
            return performedAt;
        }

        public double getHoursAtService() {
            return hoursAtService;
        }
    }

    // ------------------------------------------------------------------
    // Domain model: Crew member
    // ------------------------------------------------------------------

    public static class CrewMember {
        private final String crewId;
        private final String name;
        private final CrewRole role;
        private boolean onShift;

        public CrewMember(String crewId, String name, CrewRole role) {
            this.crewId = crewId;
            this.name = name;
            this.role = role;
            this.onShift = false;
        }

        public String getCrewId() {
            return crewId;
        }

        public String getName() {
            return name;
        }

        public CrewRole getRole() {
            return role;
        }

        public boolean isOnShift() {
            return onShift;
        }

        public void clockIn() {
            this.onShift = true;
        }

        public void clockOut() {
            this.onShift = false;
        }
    }

    // ------------------------------------------------------------------
    // Domain model: Zone (a mapped area of the mine site)
    // ------------------------------------------------------------------

    public static class Zone {
        private final String zoneId;
        private final String name;
        private int capacity;

        public Zone(String zoneId, String name, int capacity) {
            this.zoneId = zoneId;
            this.name = name;
            this.capacity = capacity;
        }

        public String getZoneId() {
            return zoneId;
        }

        public String getName() {
            return name;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }

    // ------------------------------------------------------------------
    // Domain model: Alert
    // ------------------------------------------------------------------

    public static class Alert {
        private final String alertId;
        private final String equipmentId;
        private final AlertSeverity severity;
        private final String message;
        private final Instant raisedAt;
        private boolean acknowledged;

        public Alert(String equipmentId, AlertSeverity severity, String message) {
            this.alertId = UUID.randomUUID().toString();
            this.equipmentId = equipmentId;
            this.severity = severity;
            this.message = message;
            this.raisedAt = Instant.now();
            this.acknowledged = false;
        }

        public String getAlertId() {
            return alertId;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public AlertSeverity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }

        public Instant getRaisedAt() {
            return raisedAt;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public void acknowledge() {
            this.acknowledged = true;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s (equipment=%s, ack=%s)",
                    severity, message, raisedAt, equipmentId, acknowledged);
        }
    }

    // ------------------------------------------------------------------
    // Fleet manager: orchestrates equipment, crews, zones, alerts
    // ------------------------------------------------------------------

    public static class FleetManager {
        private final Map<String, Equipment> equipmentRegistry = new HashMap<>();
        private final Map<String, CrewMember> crewRegistry = new HashMap<>();
        private final Map<String, Zone> zoneRegistry = new HashMap<>();
        private final List<Alert> alerts = new ArrayList<>();

        private static final double LOW_FUEL_THRESHOLD = 20.0;
        private static final double CRITICAL_FUEL_THRESHOLD = 5.0;
        private static final double MAINTENANCE_INTERVAL_HOURS = 250.0;

        // ---------------- Registration ----------------

        public Equipment registerEquipment(String equipmentId, EquipmentType type) {
            if (equipmentRegistry.containsKey(equipmentId)) {
                throw new IllegalArgumentException("Equipment already registered: " + equipmentId);
            }
            Equipment equipment = new Equipment(equipmentId, type);
            equipmentRegistry.put(equipmentId, equipment);
            return equipment;
        }

        public CrewMember registerCrewMember(String crewId, String name, CrewRole role) {
            if (crewRegistry.containsKey(crewId)) {
                throw new IllegalArgumentException("Crew member already registered: " + crewId);
            }
            CrewMember member = new CrewMember(crewId, name, role);
            crewRegistry.put(crewId, member);
            return member;
        }

        public Zone registerZone(String zoneId, String name, int capacity) {
            if (zoneRegistry.containsKey(zoneId)) {
                throw new IllegalArgumentException("Zone already registered: " + zoneId);
            }
            Zone zone = new Zone(zoneId, name, capacity);
            zoneRegistry.put(zoneId, zone);
            return zone;
        }

        // ---------------- Lookup helpers ----------------

        public Optional<Equipment> findEquipment(String equipmentId) {
            return Optional.ofNullable(equipmentRegistry.get(equipmentId));
        }

        public Optional<CrewMember> findCrewMember(String crewId) {
            return Optional.ofNullable(crewRegistry.get(crewId));
        }

        public Optional<Zone> findZone(String zoneId) {
            return Optional.ofNullable(zoneRegistry.get(zoneId));
        }

        public List<Equipment> allEquipment() {
            return new ArrayList<>(equipmentRegistry.values());
        }

        public List<CrewMember> allCrew() {
            return new ArrayList<>(crewRegistry.values());
        }

        // ---------------- Assignment logic ----------------

        public void assignCrewToEquipment(String crewId, String equipmentId) {
            CrewMember member = crewRegistry.get(crewId);
            Equipment equipment = equipmentRegistry.get(equipmentId);
            if (member == null) {
                throw new IllegalArgumentException("Unknown crew member: " + crewId);
            }
            if (equipment == null) {
                throw new IllegalArgumentException("Unknown equipment: " + equipmentId);
            }
            if (member.getRole() != CrewRole.OPERATOR) {
                throw new IllegalStateException("Only operators can be assigned to equipment: " + crewId);
            }
            equipment.setAssignedCrewId(crewId);
            member.clockIn();
        }

        public void unassignCrew(String equipmentId) {
            Equipment equipment = requireEquipment(equipmentId);
            String crewId = equipment.getAssignedCrewId();
            equipment.setAssignedCrewId(null);
            if (crewId != null) {
                CrewMember member = crewRegistry.get(crewId);
                if (member != null) {
                    member.clockOut();
                }
            }
        }

        public void moveEquipmentToZone(String equipmentId, String zoneId) {
            Equipment equipment = requireEquipment(equipmentId);
            Zone zone = zoneRegistry.get(zoneId);
            if (zone == null) {
                throw new IllegalArgumentException("Unknown zone: " + zoneId);
            }
            long currentOccupants = equipmentRegistry.values().stream()
                    .filter(e -> zoneId.equals(e.getCurrentZoneId()))
                    .count();
            if (currentOccupants >= zone.getCapacity()) {
                raiseAlert(equipmentId, AlertSeverity.WARNING,
                        "Zone " + zoneId + " is at capacity; move may cause congestion");
            }
            equipment.setCurrentZoneId(zoneId);
        }

        // ---------------- Operating logic ----------------

        public void startOperating(String equipmentId) {
            Equipment equipment = requireEquipment(equipmentId);
            if (equipment.getFuelLevelPercent() <= CRITICAL_FUEL_THRESHOLD) {
                raiseAlert(equipmentId, AlertSeverity.CRITICAL,
                        "Cannot start operation: fuel critically low");
                return;
            }
            equipment.setStatus(EquipmentStatus.OPERATING);
        }

        public void stopOperating(String equipmentId, double hoursOperated, double fuelConsumedPercent) {
            Equipment equipment = requireEquipment(equipmentId);
            equipment.addOperatingHours(hoursOperated);
            equipment.consumeFuel(fuelConsumedPercent);
            equipment.setStatus(EquipmentStatus.IDLE);
            checkFuelAlerts(equipment);
            checkMaintenanceDue(equipment);
        }

        public void reportDelay(String equipmentId, String reason) {
            Equipment equipment = requireEquipment(equipmentId);
            equipment.setStatus(EquipmentStatus.DELAYED);
            raiseAlert(equipmentId, AlertSeverity.WARNING, "Delay reported: " + reason);
        }

        public void reportBreakdown(String equipmentId, String reason) {
            Equipment equipment = requireEquipment(equipmentId);
            equipment.setStatus(EquipmentStatus.DOWN);
            raiseAlert(equipmentId, AlertSeverity.CRITICAL, "Breakdown reported: " + reason);
        }

        public void refuelEquipment(String equipmentId, double percent) {
            Equipment equipment = requireEquipment(equipmentId);
            equipment.refuel(percent);
        }

        // ---------------- Maintenance logic ----------------

        public void performMaintenance(String equipmentId, String description) {
            Equipment equipment = requireEquipment(equipmentId);
            MaintenanceRecord record = new MaintenanceRecord(description, equipment.getOdometerHours());
            equipment.addMaintenanceRecord(record);
            equipment.setStatus(EquipmentStatus.IDLE);
        }

        public void sendToMaintenance(String equipmentId) {
            Equipment equipment = requireEquipment(equipmentId);
            equipment.setStatus(EquipmentStatus.MAINTENANCE);
        }

        private void checkMaintenanceDue(Equipment equipment) {
            double hoursSinceLastService = hoursSinceLastMaintenance(equipment);
            if (hoursSinceLastService >= MAINTENANCE_INTERVAL_HOURS) {
                raiseAlert(equipment.getEquipmentId(), AlertSeverity.WARNING,
                        String.format("Maintenance due: %.1f hours since last service", hoursSinceLastService));
            }
        }

        private double hoursSinceLastMaintenance(Equipment equipment) {
            List<MaintenanceRecord> history = equipment.getMaintenanceHistory();
            if (history.isEmpty()) {
                return equipment.getOdometerHours();
            }
            MaintenanceRecord latest = history.get(history.size() - 1);
            return equipment.getOdometerHours() - latest.getHoursAtService();
        }

        // ---------------- Fuel logic ----------------

        private void checkFuelAlerts(Equipment equipment) {
            double fuel = equipment.getFuelLevelPercent();
            if (fuel <= CRITICAL_FUEL_THRESHOLD) {
                raiseAlert(equipment.getEquipmentId(), AlertSeverity.CRITICAL,
                        String.format("Fuel critically low: %.1f%%", fuel));
            } else if (fuel <= LOW_FUEL_THRESHOLD) {
                raiseAlert(equipment.getEquipmentId(), AlertSeverity.WARNING,
                        String.format("Fuel low: %.1f%%", fuel));
            }
        }

        // ---------------- Alerts ----------------

        public Alert raiseAlert(String equipmentId, AlertSeverity severity, String message) {
            Alert alert = new Alert(equipmentId, severity, message);
            alerts.add(alert);
            return alert;
        }

        public List<Alert> getActiveAlerts() {
            return alerts.stream()
                    .filter(a -> !a.isAcknowledged())
                    .collect(Collectors.toList());
        }

        public List<Alert> getAlertsForEquipment(String equipmentId) {
            return alerts.stream()
                    .filter(a -> a.getEquipmentId().equals(equipmentId))
                    .collect(Collectors.toList());
        }

        public void acknowledgeAlert(String alertId) {
            alerts.stream()
                    .filter(a -> a.getAlertId().equals(alertId))
                    .findFirst()
                    .ifPresent(Alert::acknowledge);
        }

        public long countCriticalAlerts() {
            return alerts.stream()
                    .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL && !a.isAcknowledged())
                    .count();
        }

        // ---------------- Fleet-wide reporting / analytics ----------------

        public double fleetUtilizationPercent() {
            if (equipmentRegistry.isEmpty()) {
                return 0.0;
            }
            long operating = equipmentRegistry.values().stream()
                    .filter(e -> e.getStatus() == EquipmentStatus.OPERATING)
                    .count();
            return (operating * 100.0) / equipmentRegistry.size();
        }

        public Map<EquipmentStatus, Long> equipmentCountByStatus() {
            return equipmentRegistry.values().stream()
                    .collect(Collectors.groupingBy(Equipment::getStatus, Collectors.counting()));
        }

        public Map<EquipmentType, Long> equipmentCountByType() {
            return equipmentRegistry.values().stream()
                    .collect(Collectors.groupingBy(Equipment::getType, Collectors.counting()));
        }

        public List<Equipment> equipmentNeedingFuel() {
            return equipmentRegistry.values().stream()
                    .filter(e -> e.getFuelLevelPercent() <= LOW_FUEL_THRESHOLD)
                    .sorted(Comparator.comparingDouble(Equipment::getFuelLevelPercent))
                    .collect(Collectors.toList());
        }

        public List<Equipment> equipmentDueForMaintenance() {
            return equipmentRegistry.values().stream()
                    .filter(e -> hoursSinceLastMaintenance(e) >= MAINTENANCE_INTERVAL_HOURS)
                    .collect(Collectors.toList());
        }

        public List<Equipment> equipmentInZone(String zoneId) {
            return equipmentRegistry.values().stream()
                    .filter(e -> zoneId.equals(e.getCurrentZoneId()))
                    .collect(Collectors.toList());
        }

        public double totalOperatingHours() {
            return equipmentRegistry.values().stream()
                    .mapToDouble(Equipment::getOdometerHours)
                    .sum();
        }

        public double averageFuelLevel() {
            return equipmentRegistry.values().stream()
                    .mapToDouble(Equipment::getFuelLevelPercent)
                    .average()
                    .orElse(0.0);
        }

        public List<Equipment> topEquipmentByHours(int limit) {
            return equipmentRegistry.values().stream()
                    .sorted(Comparator.comparingDouble(Equipment::getOdometerHours).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        public long countIdleOperators() {
            return crewRegistry.values().stream()
                    .filter(c -> c.getRole() == CrewRole.OPERATOR && !c.isOnShift())
                    .count();
        }

        public List<CrewMember> availableOperators() {
            return crewRegistry.values().stream()
                    .filter(c -> c.getRole() == CrewRole.OPERATOR && !c.isOnShift())
                    .collect(Collectors.toList());
        }

        public Optional<CrewMember> nextAvailableOperator() {
            return availableOperators().stream().findFirst();
        }

        public boolean isEquipmentUnassigned(String equipmentId) {
            Equipment equipment = requireEquipment(equipmentId);
            return equipment.getAssignedCrewId() == null;
        }

        public Duration timeSinceLastStatusChange(String equipmentId) {
            Equipment equipment = requireEquipment(equipmentId);
            return Duration.between(equipment.getLastStatusChange(), Instant.now());
        }

        // ---------------- Shift / dispatch helpers ----------------

        public void autoAssignIdleEquipment() {
            List<Equipment> idleEquipment = equipmentRegistry.values().stream()
                    .filter(e -> e.getStatus() == EquipmentStatus.IDLE && e.getAssignedCrewId() == null)
                    .collect(Collectors.toList());

            for (Equipment equipment : idleEquipment) {
                Optional<CrewMember> operator = nextAvailableOperator();
                if (operator.isPresent()) {
                    assignCrewToEquipment(operator.get().getCrewId(), equipment.getEquipmentId());
                } else {
                    break;
                }
            }
        }

        public void endOfShiftReset() {
            for (Equipment equipment : equipmentRegistry.values()) {
                if (equipment.getStatus() == EquipmentStatus.OPERATING) {
                    equipment.setStatus(EquipmentStatus.IDLE);
                }
                unassignCrew(equipment.getEquipmentId());
            }
            for (CrewMember member : crewRegistry.values()) {
                member.clockOut();
            }
        }

        public String generateFleetSummaryReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Fleet Summary Report ===\n");
            sb.append(String.format("Total equipment: %d%n", equipmentRegistry.size()));
            sb.append(String.format("Fleet utilization: %.1f%%%n", fleetUtilizationPercent()));
            sb.append(String.format("Average fuel level: %.1f%%%n", averageFuelLevel()));
            sb.append(String.format("Total operating hours: %.1f%n", totalOperatingHours()));
            sb.append(String.format("Active alerts: %d (critical: %d)%n",
                    getActiveAlerts().size(), countCriticalAlerts()));
            sb.append("Status breakdown:\n");
            equipmentCountByStatus().forEach((status, count) ->
                    sb.append(String.format("  %s: %d%n", status, count)));
            sb.append("Type breakdown:\n");
            equipmentCountByType().forEach((type, count) ->
                    sb.append(String.format("  %s: %d%n", type, count)));
            return sb.toString();
        }

        private Equipment requireEquipment(String equipmentId) {
            Equipment equipment = equipmentRegistry.get(equipmentId);
            if (equipment == null) {
                throw new IllegalArgumentException("Unknown equipment: " + equipmentId);
            }
            return equipment;
        }
    }

    // ------------------------------------------------------------------
    // Demo entry point
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        FleetManager fleet = new FleetManager();

        // Register zones
        fleet.registerZone("ZONE-PIT-A", "Pit A - North Bench", 6);
        fleet.registerZone("ZONE-PIT-B", "Pit B - South Bench", 4);
        fleet.registerZone("ZONE-CRUSHER", "Primary Crusher Pad", 3);
        fleet.registerZone("ZONE-WORKSHOP", "Maintenance Workshop", 5);

        // Register equipment
        fleet.registerEquipment("HT-101", EquipmentType.HAUL_TRUCK);
        fleet.registerEquipment("HT-102", EquipmentType.HAUL_TRUCK);
        fleet.registerEquipment("EX-201", EquipmentType.EXCAVATOR);
        fleet.registerEquipment("DZ-301", EquipmentType.DOZER);
        fleet.registerEquipment("DR-401", EquipmentType.DRILL);
        fleet.registerEquipment("WC-501", EquipmentType.WATER_CART);

        // Register crew
        fleet.registerCrewMember("C-01", "R. Sharma", CrewRole.OPERATOR);
        fleet.registerCrewMember("C-02", "A. Verma", CrewRole.OPERATOR);
        fleet.registerCrewMember("C-03", "S. Iyer", CrewRole.SUPERVISOR);
        fleet.registerCrewMember("C-04", "K. Nair", CrewRole.MAINTENANCE_TECH);
        fleet.registerCrewMember("C-05", "P. Das", CrewRole.DISPATCHER);
        fleet.registerCrewMember("C-06", "M. Rao", CrewRole.OPERATOR);

        // Move equipment into zones
        fleet.moveEquipmentToZone("HT-101", "ZONE-PIT-A");
        fleet.moveEquipmentToZone("HT-102", "ZONE-PIT-A");
        fleet.moveEquipmentToZone("EX-201", "ZONE-PIT-B");
        fleet.moveEquipmentToZone("DZ-301", "ZONE-PIT-B");
        fleet.moveEquipmentToZone("DR-401", "ZONE-PIT-A");
        fleet.moveEquipmentToZone("WC-501", "ZONE-CRUSHER");

        // Auto assign operators to idle equipment
        fleet.autoAssignIdleEquipment();

        // Start operations
        fleet.startOperating("HT-101");
        fleet.startOperating("HT-102");
        fleet.startOperating("EX-201");

        // Simulate a shift of work
        fleet.stopOperating("HT-101", 6.5, 35.0);
        fleet.stopOperating("HT-102", 7.0, 40.0);
        fleet.stopOperating("EX-201", 8.0, 20.0);

        // Report a delay and a breakdown
        fleet.reportDelay("DZ-301", "waiting on blast clearance");
        fleet.reportBreakdown("DR-401", "hydraulic pressure fault");

        // Refuel low equipment
        for (Equipment equipment : fleet.equipmentNeedingFuel()) {
            fleet.refuelEquipment(equipment.getEquipmentId(), 60.0);
        }

        // Perform maintenance on the down drill
        fleet.sendToMaintenance("DR-401");
        fleet.performMaintenance("DR-401", "Replaced hydraulic hose and refilled fluid");

        // Print active alerts
        System.out.println("Active alerts:");
        for (Alert alert : fleet.getActiveAlerts()) {
            System.out.println("  " + alert);
        }

        // Acknowledge all warning-level alerts
        for (Alert alert : fleet.getActiveAlerts()) {
            if (alert.getSeverity() == AlertSeverity.WARNING) {
                fleet.acknowledgeAlert(alert.getAlertId());
            }
        }

        // Print fleet summary
        System.out.println();
        System.out.println(fleet.generateFleetSummaryReport());

        // End of shift
        fleet.endOfShiftReset();
        System.out.println("End of shift reset complete.");
    }
}