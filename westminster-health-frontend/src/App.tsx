import { useState } from "react";
import { StatsCards } from "@/components/StatsCards";
import { StaffTable } from "@/components/StaffTable";
import { useHealthCentreData } from "@/hooks/useStaff";

/**
 * Root component — owns all application data.
 *
 * Data flows in one direction:
 *   App (owns state) → StatsCards / StaffTable (display + mutate)
 *
 * After any mutation, child components call onMutate() which triggers
 * a fresh fetch of both staff and stats. No libraries needed — this is
 * standard React prop-driven data flow.
 */
function App() {
  const { staff, stats, loading, error, refresh } = useHealthCentreData();

  return (
    <div className="min-h-screen bg-background">
      {/* ── Header ── */}
      <header className="border-b bg-white sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-4">
          <h1 className="text-xl font-bold tracking-tight">
            Westminster Health Centre
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Staff Management System
          </p>
        </div>
      </header>

      {/* ── Main ── */}
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8 space-y-8">
        <StatsCards stats={stats} loading={loading} />
        <StaffTable
          staff={staff}
          loading={loading}
          error={error}
          onMutate={refresh}
        />
      </main>
    </div>
  );
}

export default App;
