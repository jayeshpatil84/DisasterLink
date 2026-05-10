# DisasterLink — Interview Cheat Sheet

## The 60-Second Pitch

> "I built DisasterLink because SACHET tells you a flood is coming, UN OCHA coordinates governments — but there's no platform for the actual ground-level rescue loop. A victim sends an SOS, the system AI-triages it in under 2 seconds using Gemini, auto-assigns the nearest skilled volunteer using Haversine distance calculation, and a district officer sees everything in real-time on a Leaflet.js map via WebSocket. SMS fallback means it works even on 2G when data is gone."

---

## Architecture Questions

**Q: Why Redis instead of Kafka?**
> Kafka requires ZooKeeper and cluster config — complex for a solo prototype. I designed the MessageBrokerService with a Kafka-compatible interface: same `publish(topic, payload)` signature. Redis Pub/Sub handles the prototype. In production with thousands of simultaneous SOS signals during a major cyclone, you swap in KafkaTemplate with zero business logic changes — same topic names, same contract.

**Q: How does the real-time map work?**
> When a SOS is submitted: (1) saved to MySQL immediately, (2) broadcast via STOMP WebSocket to all connected dashboards, (3) AI triage runs async via Gemini API, (4) updated beacon re-broadcast. The Leaflet.js map subscribes to `/topic/sos` and adds a color-coded CircleMarker — CRITICAL beacons pulse red, ASSIGNED ones turn blue.

**Q: How does volunteer assignment work?**
> I use Haversine formula for great-circle distance between volunteer's GPS and the SOS beacon. I start with a 10km search radius, expand to 50km if no match. I also skill-match: FLOOD → BOAT_OPERATOR, EARTHQUAKE → STRUCTURAL_ENGINEER, injury keywords → MEDICAL. The nearest available volunteer with the right skill gets the assignment via SMS.

**Q: What's the fallback if Gemini API is down?**
> Rule-based triage: keyword scoring (trapped, unconscious, bleeding = +40 points), people count scoring (50+ = +30), disaster type weights. Critical keywords + 50+ people = CRITICAL automatically. Interviewers love this because it shows resilience thinking — in a real disaster, APIs go down.

**Q: How is security implemented?**
> JWT + Spring Security + RBAC with 4 roles: VICTIM (send SOS only), VOLUNTEER (view beacons, update location), DISTRICT_OFFICER (full dashboard), ADMIN (everything). JWT is stateless — no sessions. Each role's endpoints are annotated with `@PreAuthorize`.

**Q: Why not use LangChain?**
> LangChain adds 200MB+ of dependencies and abstraction layers that complicate debugging. Gemini's REST API is clean and the prompt engineering is simple enough to control directly. For a production system I'd reconsider, but for a prototype it adds zero value and significant overhead.

---

## Numbers to Remember

| Metric | Value |
|--------|-------|
| SOS → Map pin latency | < 500ms |
| AI triage response | 1-3 seconds (async, non-blocking) |
| Volunteer search start radius | 10km |
| Volunteer search max radius | 50km |
| JWT expiry | 24 hours |
| Initial Redis topics | 4 |
| RBAC roles | 4 |
| Core features built | 5 |

---

## What "Partially Implemented" Means (If Asked)

- **Resource prediction**: designed the data model (DisasterZone), would use cluster density of SOS beacons to predict peak resource need
- **PDF reports**: Spring's ReportService stub is there, would use iText/JasperReports
- **Admin dashboard**: user management endpoints exist in SecurityConfig, just no UI page built

---

## The "Why This Project" Answer

> "I wanted to build something that's genuinely missing — not a to-do app or e-commerce clone. India has SACHET for alerts but nothing that closes the loop between a stranded victim, a skilled volunteer, and an officer who needs to coordinate 50 simultaneous events. The real-time architecture, AI triage, and SMS fallback are all deliberate choices for the India context: network degrades during disasters, not everyone has app access, and district officers need a single-screen view they can act on immediately."
