/**
 * The tenant and its configuration: org, scan_point, program, opening hours, GPS.
 *
 * <p>{@code program} (discipline) lives here, not under {@code member}: it is part of how an owner
 * configures a site, is created in the F1 wizard, and {@code scan_point.program_id} points to it
 * (D7). The {@code member_program} join table belongs to {@code member}.
 */
package com.checkino.org;
