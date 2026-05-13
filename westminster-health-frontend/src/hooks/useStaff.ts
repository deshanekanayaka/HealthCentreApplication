import { useState, useEffect, useCallback } from "react";
import { fetchAllStaff, fetchStats } from "@/api/staffApi";
import type { StaffMember, StaffStats } from "@/types/staff";

/**
 * Custom hook that owns all server data for the application.
 *
 * Fetches the staff list and stats in parallel on mount, and exposes a
 * {@code refresh} function that any component can call after a mutation
 * (add, edit, delete) to re-sync the UI with the server.
 *
 * Lifting this logic into a single hook means:
 * - App.tsx stays clean — it just passes data and refresh down as props
 * - Components don't fetch independently — there's one source of truth
 * - No external libraries needed — useState + useEffect + useCallback
 *   are sufficient for this scale
 */
export function useHealthCentreData() {
  const [staff, setStaff]     = useState<StaffMember[]>([]);
  const [stats, setStats]     = useState<StaffStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string | null>(null);

  /**
   * Fetches staff and stats in parallel.
   * useCallback ensures the function reference is stable — safe to put in
   * a useEffect dependency array without causing infinite re-renders.
   */
  const refresh = useCallback(async () => {
    setError(null);
    try {
      // Promise.all fires both requests simultaneously rather than waiting
      // for the first to finish before starting the second.
      const [staffData, statsData] = await Promise.all([
        fetchAllStaff(),
        fetchStats(),
      ]);
      setStaff(staffData);
      setStats(statsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load data.");
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch on mount — the empty-ish dependency array (just stable `refresh`)
  // means this runs exactly once when the component tree first mounts.
  useEffect(() => {
    refresh();
  }, [refresh]);

  return { staff, stats, loading, error, refresh };
}
