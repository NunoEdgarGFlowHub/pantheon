package tech.pegasys.pantheon.ethereum.eth.manager;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;

public class PeerReputation {
  private static final Logger LOG = getLogger();
  private static final int TIMEOUT_THRESHOLD = 3;
  private static final int USELESS_RESPONSE_THRESHOLD = 5;
  static final long USELESS_RESPONSE_WINDOW_IN_MILLIS =
      TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);

  private final ConcurrentMap<Integer, AtomicInteger> timeoutCountByRequestType =
      new ConcurrentHashMap<>();
  private final Queue<Long> uselessResponseTimes = new ConcurrentLinkedQueue<>();

  public Optional<DisconnectReason> recordRequestTimeout(final int requestCode) {
    final int newTimeoutCount = getOrCreateTimeoutCount(requestCode).incrementAndGet();
    if (newTimeoutCount >= TIMEOUT_THRESHOLD) {
      LOG.info("Disconnection triggered by repeated timeouts");
      return Optional.of(DisconnectReason.TIMEOUT);
    } else {
      return Optional.empty();
    }
  }

  public void resetTimeoutCount(final int requestCode) {
    timeoutCountByRequestType.remove(requestCode);
  }

  private AtomicInteger getOrCreateTimeoutCount(final int requestCode) {
    return timeoutCountByRequestType.computeIfAbsent(requestCode, code -> new AtomicInteger());
  }

  public Map<Integer, AtomicInteger> timeoutCounts() {
    return timeoutCountByRequestType;
  }

  public Optional<DisconnectReason> recordUselessResponse(final long timestamp) {
    uselessResponseTimes.add(timestamp);
    while (shouldRemove(uselessResponseTimes.peek(), timestamp)) {
      uselessResponseTimes.poll();
    }
    if (uselessResponseTimes.size() >= USELESS_RESPONSE_THRESHOLD) {
      LOG.info("Disconnection triggered by exceeding useless response threshold");
      return Optional.of(DisconnectReason.USELESS_PEER);
    } else {
      return Optional.empty();
    }
  }

  private boolean shouldRemove(final Long timestamp, final long currentTimestamp) {
    return timestamp != null && timestamp + USELESS_RESPONSE_WINDOW_IN_MILLIS < currentTimestamp;
  }
}