/**
 * Two separate auth needs: magic link (owner/staff) and device token (member).
 *
 * <p>Owner/staff use an email magic link; members use a device token in an httpOnly cookie with a
 * 1-year TTL. Email for members is wrong for this market (D3). v1 does NOT send OTP to members
 * (D2).
 */
package com.checkino.auth;
