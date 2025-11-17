"""
User concurrency lock manager for intervention agent.

Provides environment-aware locking:
- Development: In-memory lock (single instance)
- Production: Redis-based distributed lock (multi-instance)
"""

import os
import time
from abc import ABC, abstractmethod
from typing import Optional
import logging

logger = logging.getLogger(__name__)


class LockManager(ABC):
    """Abstract base class for user concurrency locks."""

    @abstractmethod
    def acquire_lock(self, user_id: str, ttl: int = 60) -> bool:
        """
        Try to acquire a lock for the given user_id.

        Args:
            user_id: User's personalId
            ttl: Time-to-live in seconds (auto-release after this time)

        Returns:
            True if lock acquired, False if user is already locked
        """
        pass

    @abstractmethod
    def release_lock(self, user_id: str) -> None:
        """
        Release the lock for the given user_id.

        Args:
            user_id: User's personalId
        """
        pass


class InMemoryLockManager(LockManager):
    """
    In-memory lock manager for development environment.

    Simple dict-based implementation with TTL tracking.
    Note: Only works for single instance deployments.
    """

    def __init__(self):
        self._locks: dict[str, float] = {}  # user_id -> expiry_timestamp
        logger.info("Initialized InMemoryLockManager (development mode)")

    def acquire_lock(self, user_id: str, ttl: int = 60) -> bool:
        """Acquire lock using in-memory dict."""
        current_time = time.time()

        # Clean up expired locks
        self._cleanup_expired_locks(current_time)

        # Check if user already has an active lock
        if user_id in self._locks:
            logger.info(f"Lock acquisition failed for user {user_id}: already locked")
            return False

        # Acquire lock with TTL
        expiry_time = current_time + ttl
        self._locks[user_id] = expiry_time
        logger.info(f"Lock acquired for user {user_id} (TTL: {ttl}s)")
        return True

    def release_lock(self, user_id: str) -> None:
        """Release lock by removing from dict."""
        if user_id in self._locks:
            del self._locks[user_id]
            logger.info(f"Lock released for user {user_id}")
        else:
            logger.warning(f"Attempted to release non-existent lock for user {user_id}")

    def _cleanup_expired_locks(self, current_time: float) -> None:
        """Remove expired locks from memory."""
        expired_users = [
            user_id for user_id, expiry in self._locks.items()
            if expiry <= current_time
        ]
        for user_id in expired_users:
            del self._locks[user_id]
            logger.debug(f"Cleaned up expired lock for user {user_id}")


class RedisLockManager(LockManager):
    """
    Redis-based distributed lock manager for production environment.

    Uses Redis SET NX EX for atomic lock acquisition with TTL.
    Supports multi-instance deployments.
    """

    def __init__(self, redis_uri: Optional[str] = None):
        """
        Initialize Redis lock manager.

        Args:
            redis_uri: Redis connection URI (default: from REDIS_URI env var)
        """
        try:
            import redis

            uri = redis_uri or os.getenv("REDIS_URI", "redis://redis:6379")
            self._client = redis.from_url(uri, decode_responses=True)

            # Test connection
            self._client.ping()
            logger.info(f"Initialized RedisLockManager (production mode) - connected to {uri}")

        except ImportError:
            logger.error("redis package not installed - falling back to InMemoryLockManager")
            raise
        except Exception as e:
            logger.error(f"Failed to connect to Redis: {e} - falling back to InMemoryLockManager")
            raise

    def acquire_lock(self, user_id: str, ttl: int = 60) -> bool:
        """
        Acquire lock using Redis SET NX EX (atomic operation).

        SET key value NX EX ttl returns:
        - True if key was set (lock acquired)
        - False if key already exists (lock failed)
        """
        lock_key = self._get_lock_key(user_id)

        try:
            # SET NX (not exists) EX (expiry in seconds)
            acquired = self._client.set(
                lock_key,
                "locked",
                nx=True,  # Only set if not exists
                ex=ttl    # Auto-expire after TTL seconds
            )

            if acquired:
                logger.info(f"Lock acquired for user {user_id} (TTL: {ttl}s)")
            else:
                logger.info(f"Lock acquisition failed for user {user_id}: already locked")

            return bool(acquired)

        except Exception as e:
            logger.error(f"Redis lock acquisition error for user {user_id}: {e}")
            # Fail-safe: return False to prevent duplicate processing
            return False

    def release_lock(self, user_id: str) -> None:
        """Release lock by deleting the Redis key."""
        lock_key = self._get_lock_key(user_id)

        try:
            deleted = self._client.delete(lock_key)
            if deleted:
                logger.info(f"Lock released for user {user_id}")
            else:
                logger.warning(f"Attempted to release non-existent lock for user {user_id}")

        except Exception as e:
            logger.error(f"Redis lock release error for user {user_id}: {e}")

    def _get_lock_key(self, user_id: str) -> str:
        """Generate Redis key for user lock."""
        return f"intervention:user:{user_id}:lock"


def get_lock_manager() -> LockManager:
    """
    Factory function to get appropriate lock manager based on environment.

    Returns:
        RedisLockManager if REDIS_URI is set, otherwise InMemoryLockManager
    """
    redis_uri = os.getenv("REDIS_URI")

    if redis_uri:
        try:
            return RedisLockManager(redis_uri)
        except Exception as e:
            logger.warning(f"Failed to initialize Redis lock manager: {e}")
            logger.warning("Falling back to InMemoryLockManager")
            return InMemoryLockManager()
    else:
        logger.info("REDIS_URI not set - using InMemoryLockManager for development")
        return InMemoryLockManager()
