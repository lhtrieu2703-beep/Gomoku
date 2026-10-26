package game;

import game.network.NetworkManager;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class NetworkStabilityTest {
    static final class Events implements NetworkManager.NetworkListener {
        final BlockingQueue<String> events = new LinkedBlockingQueue<>();
        final BlockingQueue<Integer> ports = new LinkedBlockingQueue<>();
        public void onHosting(int port) { ports.add(port); }
        public void onConnected(boolean host) { events.add("connected:" + host); }
        public void onMoveReceived(int r, int c) { events.add("move:" + r + ":" + c); }
        public void onRestartRequested() { events.add("restart"); }
        public void onOpponentDisconnected() { events.add("disconnected"); }
        public void onError(NetworkManager.NetworkError error) { events.add("error:" + error.type()); }
        String next() throws InterruptedException { return events.poll(3, TimeUnit.SECONDS); }
    }

    @Test
    void cancelPendingHostReleasesPortWithoutError() throws Exception {
        Events events = new Events();
        NetworkManager manager = new NetworkManager(events);
        try {
            manager.hostGame(0);
            Integer port = events.ports.poll(3, TimeUnit.SECONDS);
            assertNotNull(port);
            manager.disconnect();
            try (ServerSocket rebound = new ServerSocket(port)) {
                assertTrue(rebound.isBound());
            }
            assertFalse(manager.isConnected());
            assertNull(events.events.poll(150, TimeUnit.MILLISECONDS));
        } finally { manager.disconnect(); }
    }

    @Test
    void hostAndClientExchangeMovesRestartAndDisconnect() throws Exception {
        Events hostEvents = new Events();
        Events clientEvents = new Events();
        NetworkManager host = new NetworkManager(hostEvents);
        NetworkManager client = new NetworkManager(clientEvents);
        try {
            host.hostGame(0);
            Integer port = hostEvents.ports.poll(3, TimeUnit.SECONDS);
            assertNotNull(port);
            client.joinGame("127.0.0.1", port);
            assertEquals("connected:true", hostEvents.next());
            assertEquals("connected:false", clientEvents.next());
            host.sendMove(7, 7);
            assertEquals("move:7:7", clientEvents.next());
            client.sendMove(6, 6);
            assertEquals("move:6:6", hostEvents.next());
            host.sendRestart();
            assertEquals("restart", clientEvents.next());
            client.disconnect();
            assertEquals("disconnected", hostEvents.next());
            assertFalse(host.isConnected());
        } finally { client.disconnect(); host.disconnect(); }
    }

    @Test
    void malformedMoveDoesNotKillConnectionAndOrderIsPreserved() throws Exception {
        Events events = new Events();
        NetworkManager manager = new NetworkManager(events);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000);
            manager.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                assertEquals("connected:false", events.next());
                PrintWriter out = new PrintWriter(peer.getOutputStream(), true, StandardCharsets.UTF_8);
                out.println("MOVE nope 4");
                out.println("MOVE 1 2 3");
                out.println("MOVE 4 5");
                out.println("RESTART");
                out.println("MOVE   6   7");
                assertEquals("move:4:5", events.next());
                assertEquals("restart", events.next());
                assertEquals("move:6:7", events.next());
                assertTrue(manager.isConnected());
            }
        } finally { manager.disconnect(); }
    }

    @Test
    void callbacksAlreadyQueuedAreDiscardedAfterCancel() throws Exception {
        Events events = new Events();
        BlockingQueue<Runnable> callbacks = new LinkedBlockingQueue<>();
        NetworkManager manager = new NetworkManager(events, callbacks::add);
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(3000);
            manager.joinGame("127.0.0.1", server.getLocalPort());
            try (Socket peer = server.accept()) {
                Runnable connected = callbacks.poll(3, TimeUnit.SECONDS);
                assertNotNull(connected);
                new PrintWriter(peer.getOutputStream(), true).println("MOVE 1 2");
                Runnable move = callbacks.poll(3, TimeUnit.SECONDS);
                assertNotNull(move);
                manager.disconnect();
                connected.run();
                move.run();
                assertTrue(events.events.isEmpty());
            }
        } finally { manager.disconnect(); }
    }

    @Test
    void reconnectCannotBeClosedOrNotifiedByPreviousSession() throws Exception {
        Events events = new Events();
        BlockingQueue<Runnable> callbacks = new LinkedBlockingQueue<>();
        NetworkManager manager = new NetworkManager(events, callbacks::add);
        try (ServerSocket first = new ServerSocket(0); ServerSocket second = new ServerSocket(0)) {
            first.setSoTimeout(3000); second.setSoTimeout(3000);
            manager.joinGame("127.0.0.1", first.getLocalPort());
            try (Socket oldPeer = first.accept()) {
                Runnable oldConnected = callbacks.poll(3, TimeUnit.SECONDS);
                assertNotNull(oldConnected);
                manager.joinGame("127.0.0.1", second.getLocalPort());
                try (Socket newPeer = second.accept()) {
                    Runnable newConnected = callbacks.poll(3, TimeUnit.SECONDS);
                    assertNotNull(newConnected);
                    oldConnected.run(); newConnected.run();
                    assertEquals("connected:false", events.next());
                    oldPeer.setSoTimeout(3000);
                    assertEquals(-1, oldPeer.getInputStream().read());
                    new PrintWriter(newPeer.getOutputStream(), true).println("MOVE 8 9");
                    Runnable move = callbacks.poll(3, TimeUnit.SECONDS);
                    assertNotNull(move);
                    move.run();
                    assertEquals("move:8:9", events.next());
                    assertTrue(manager.isConnected());
                    assertTrue(events.events.isEmpty());
                }
            }
        } finally { manager.disconnect(); }
    }

    @Test
    void staleErrorIsDiscardedWhenNewSessionStarts() throws Exception {
        Events events = new Events();
        BlockingQueue<Runnable> callbacks = new LinkedBlockingQueue<>();
        NetworkManager manager = new NetworkManager(events, callbacks::add);
        try (ServerSocket occupied = new ServerSocket(0); ServerSocket next = new ServerSocket(0)) {
            next.setSoTimeout(3000);
            manager.hostGame(occupied.getLocalPort());
            Runnable error = callbacks.poll(3, TimeUnit.SECONDS);
            assertNotNull(error);
            manager.joinGame("127.0.0.1", next.getLocalPort());
            try (Socket peer = next.accept()) {
                Runnable connected = callbacks.poll(3, TimeUnit.SECONDS);
                assertNotNull(connected);
                error.run(); connected.run();
                assertEquals("connected:false", events.next());
                assertTrue(events.events.isEmpty());
                assertTrue(manager.isConnected());
            }
        } finally { manager.disconnect(); }
    }

    @Test
    void occupiedPortReportsTypedHostError() throws Exception {
        Events events = new Events();
        NetworkManager manager = new NetworkManager(events);
        try (ServerSocket occupied = new ServerSocket(0)) {
            manager.hostGame(occupied.getLocalPort());
            assertEquals("error:HOST", events.next());
            assertFalse(manager.isConnected());
        } finally { manager.disconnect(); }
    }
}
