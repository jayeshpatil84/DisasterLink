/**
 * Sample beacons displayed when the backend is not reachable or the user
 * is not yet authenticated. This makes the dashboard immediately visually
 * meaningful without requiring a live backend connection.
 *
 * FIX L5: Extracted from dashboard.ts and sos-list.ts to eliminate duplication.
 * The same data was copy-pasted across both components — a DRY violation that
 * meant any sample data change required editing two files.
 */
import { SosBeacon } from './sos-beacon.model';

export const SAMPLE_BEACONS: SosBeacon[] = [
  {
    id: 101,
    description:
      'Flash flood water entering ground floor homes. 4 family members trapped on roof needing immediate boat rescue.',
    disasterType: 'FLOOD',
    latitude: 19.076,
    longitude: 72.8777,
    address: 'Kurla West, Mumbai, Maharashtra',
    urgencyScore: 92,
    urgencyLabel: 'CRITICAL',
    triageNote: 'Gemini AI: High risk of drowning. Priority 1 evacuation recommended.',
    status: 'PENDING',
    reporterId: 1,
    reporterUsername: 'victim_mumbai',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 102,
    description:
      'Massive landslide blocking main highway. Electric poles down with sparking live wires near residential colony.',
    disasterType: 'LANDSLIDE',
    latitude: 11.605,
    longitude: 76.083,
    address: 'Meppadi, Wayanad, Kerala',
    urgencyScore: 85,
    urgencyLabel: 'HIGH',
    triageNote: 'Gemini AI: Road blockage & electrical hazard detected.',
    status: 'IN_PROGRESS',
    reporterId: 2,
    reporterUsername: 'kerala_citizen',
    volunteerId: 5,
    volunteerUsername: 'rescue_vol_1',
    createdAt: new Date(Date.now() - 3_600_000).toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 103,
    description:
      'Commercial building fire spreading to adjacent apartments. Thick smoke reported on 3rd floor.',
    disasterType: 'FIRE',
    latitude: 28.6139,
    longitude: 77.209,
    address: 'Connaught Place, New Delhi',
    urgencyScore: 78,
    urgencyLabel: 'HIGH',
    triageNote: 'Gemini AI: Severe fire & smoke inhalation risk.',
    status: 'PENDING',
    reporterId: 3,
    reporterUsername: 'delhi_resident',
    createdAt: new Date(Date.now() - 7_200_000).toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 104,
    description: 'Severe earthquake tremors caused wall collapse in old structure. Minor injuries reported.',
    disasterType: 'EARTHQUAKE',
    latitude: 23.0225,
    longitude: 72.5714,
    address: 'Ellisbridge, Ahmedabad, Gujarat',
    urgencyScore: 55,
    urgencyLabel: 'MEDIUM',
    triageNote: 'Gemini AI: Structural damage; non-life-threatening injuries.',
    status: 'RESOLVED',
    reporterId: 4,
    reporterUsername: 'gujarat_user',
    createdAt: new Date(Date.now() - 14_400_000).toISOString(),
    updatedAt: new Date().toISOString(),
  },
];