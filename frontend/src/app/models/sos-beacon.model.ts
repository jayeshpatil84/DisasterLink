// SOS Beacon domain models

export type DisasterType =
  | 'FLOOD' | 'FIRE' | 'EARTHQUAKE' | 'CYCLONE'
  | 'LANDSLIDE' | 'TSUNAMI' | 'ACCIDENT' | 'MEDICAL' | 'OTHER';

export type UrgencyLabel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type BeaconStatus =
  | 'PENDING'
  | 'ASSIGNED'
  | 'EN_ROUTE'
  | 'ARRIVED'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CANCELLED';

export interface SosRequest {
  description: string;
  disasterType: DisasterType;
  latitude: number;
  longitude: number;
  address?: string;
}

export interface SosBeacon {
  id: number;
  description: string;
  disasterType: DisasterType;
  latitude: number;
  longitude: number;
  address?: string;
  urgencyScore: number;
  urgencyLabel: UrgencyLabel;
  triageNote?: string;
  status: BeaconStatus;
  reporterId: number;
  reporterUsername: string;
  volunteerId?: number;
  volunteerUsername?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardStats {
  totalBeacons: number;
  criticalBeacons: number;
  highBeacons: number;
  pendingBeacons: number;
  assignedBeacons: number;
  inProgressBeacons: number;
  resolvedBeacons: number;
  totalVolunteers: number;
  activeVolunteers: number;
  availableVolunteers: number;
  busyVolunteers: number;
}

export interface Volunteer {
  id: number;
  username: string;
}

export interface VolunteerInfo {
  id: number;
  username: string;
  volunteerStatus: 'AVAILABLE' | 'BUSY' | 'OFFLINE';
  activeTaskCount: number;
}

export interface SosStatusHistory {
  id: number;
  sosId: number;
  oldStatus?: string;
  newStatus: string;
  changedBy: string;
  changedAt: string;
}

export interface WebSocketNotification {
  sosId: number;
  eventType: string;
  message: string;
}

/** Maps urgency label to a CSS class name used for color coding. */
export const URGENCY_CLASS: Record<UrgencyLabel, string> = {
  CRITICAL: 'urgency-critical',
  HIGH:     'urgency-high',
  MEDIUM:   'urgency-medium',
  LOW:      'urgency-low',
};

/** Maps urgency label to a display emoji. */
export const URGENCY_EMOJI: Record<UrgencyLabel, string> = {
  CRITICAL: '🚨',
  HIGH:     '⚠️',
  MEDIUM:   '🟡',
  LOW:      '🟢',
};

/** Maps disaster type to a display emoji. */
export const DISASTER_EMOJI: Record<DisasterType, string> = {
  FLOOD:      '🌊',
  FIRE:       '🔥',
  EARTHQUAKE: '🌍',
  CYCLONE:    '🌀',
  LANDSLIDE:  '⛰️',
  TSUNAMI:    '🌊',
  ACCIDENT:   '🚗',
  MEDICAL:    '🏥',
  OTHER:      '⚡',
};
