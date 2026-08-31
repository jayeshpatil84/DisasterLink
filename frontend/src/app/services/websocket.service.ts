import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import { environment } from '../../environments/environment';
import { SosBeacon, WebSocketNotification } from '../models/sos-beacon.model';

/**
 * Manages the STOMP WebSocket connection to the backend.
 * Supports multiple topic subscriptions and auto-reconnection.
 */
@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {

  private client: Client;
  private beaconSubject = new Subject<SosBeacon>();
  private notificationSubject = new Subject<{ topic: string; data: any }>();
  private connected = false;

  constructor() {
    this.client = new Client({
      webSocketFactory: () => {
        return new SockJS(environment.wsUrl);
      },
      reconnectDelay: 5000,
      onConnect: () => {
        this.connected = true;
        console.log('[WebSocket] Connected');

        // Default feed subscription
        this.client.subscribe('/topic/sos-feed', (message: IMessage) => {
          try {
            const beacon: SosBeacon = JSON.parse(message.body);
            this.beaconSubject.next(beacon);
          } catch (error) {
            console.warn('[WebSocket] Failed to parse beacon message', error);
          }
        });
      },
      onDisconnect: () => {
        this.connected = false;
        console.log('[WebSocket] Disconnected');
      },
      onStompError: (frame) => {
        console.error('[WebSocket] Broker error:', frame.headers['message'], frame.body);
      },
      onWebSocketError: (event) => {
        console.error('[WebSocket] Transport error:', event);
      }
    });
  }

  connect(): void {
    if (typeof window !== 'undefined' && !this.client.active) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
      this.connected = false;
    }
  }

  /**
   * Subscribes to any arbitrary STOMP topic and returns an Observable of parsed payloads.
   */
  subscribeToTopic<T = any>(topic: string): Observable<T> {
    const subject = new Subject<T>();

    const checkAndSub = () => {
      if (this.client.connected) {
        this.client.subscribe(topic, (message: IMessage) => {
          try {
            const parsed: T = JSON.parse(message.body);
            subject.next(parsed);
          } catch (e) {
            console.warn(`[WebSocket] Error parsing message from ${topic}`, e);
          }
        });
      } else {
        setTimeout(checkAndSub, 500);
      }
    };

    checkAndSub();
    return subject.asObservable();
  }

  /**
   * Observable stream of SOS beacons from /topic/sos-feed.
   */
  getBeaconFeed(): Observable<SosBeacon> {
    return this.beaconSubject.asObservable();
  }

  /**
   * Subscribe to new SOS events (Officer topic).
   */
  getNewSosNotifications(): Observable<WebSocketNotification> {
    return this.subscribeToTopic<WebSocketNotification>('/topic/sos/new');
  }

  /**
   * Subscribe to resolved SOS events (Officer topic).
   */
  getResolvedSosNotifications(): Observable<WebSocketNotification> {
    return this.subscribeToTopic<WebSocketNotification>('/topic/sos/resolved');
  }

  /**
   * Subscribe to volunteer-specific task notifications.
   */
  getVolunteerNotifications(volunteerId: number): Observable<WebSocketNotification> {
    return this.subscribeToTopic<WebSocketNotification>(`/topic/volunteer/${volunteerId}/task`);
  }

  /**
   * Subscribe to victim-specific SOS update notifications.
   */
  getVictimNotifications(victimId: number): Observable<WebSocketNotification> {
    return this.subscribeToTopic<WebSocketNotification>(`/topic/victim/${victimId}/sos-update`);
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.beaconSubject.complete();
    this.notificationSubject.complete();
  }
}
