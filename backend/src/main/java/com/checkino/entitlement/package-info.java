/**
 * Session packs / membership cards and the session-consumption policy.
 *
 * <p>Consuming a session must happen in the same transaction as the check-in insert (mechanism 1).
 * This is the hardest reason microservices were ruled out (D5).
 */
package com.checkino.entitlement;
