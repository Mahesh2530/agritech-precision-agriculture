import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

/**
 * Subscribes to one or more STOMP topics and calls onMessage(topic, payload) as
 * messages arrive. Used for live telemetry, irrigation, and alert updates.
 *
 * Example:
 *   useWebSocket(['/topic/telemetry/1', '/topic/alerts/1'], (topic, msg) => { ... })
 */
export function useWebSocket(topics, onMessage) {
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    if (!topics || topics.length === 0) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 4000,
      onConnect: () => {
        setConnected(true)
        topics.forEach((topic) => {
          client.subscribe(topic, (message) => {
            try {
              const payload = JSON.parse(message.body)
              onMessage(topic, payload)
            } catch {
              onMessage(topic, message.body)
            }
          })
        })
      },
      onDisconnect: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(topics)])

  return { connected }
}
