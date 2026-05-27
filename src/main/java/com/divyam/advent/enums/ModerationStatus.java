package com.divyam.advent.enums;

/**
 * Pre-approval moderation state for a user's completed challenge.
 *
 * <ul>
 *   <li>PENDING (default after proof upload) — awaiting admin decision. Visible
 *       to the user as "in review"; does not count toward streak/win rate.</li>
 *   <li>APPROVED — admin confirmed the proof; counts as a real completion for
 *       all stats.</li>
 *   <li>REJECTED — admin denied the proof; counts as a loss against win rate
 *       (treated like {@code EXPIRED}).</li>
 * </ul>
 */
public enum ModerationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
