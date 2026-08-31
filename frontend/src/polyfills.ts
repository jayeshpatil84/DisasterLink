// Must run before SockJS / STOMP are imported.
(window as typeof window & { global?: Window }).global = window;
