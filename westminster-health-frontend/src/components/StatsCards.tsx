import { Users, Stethoscope, MonitorCheck, AlertCircle } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import type { StaffStats } from "@/types/staff";

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  accent: string;
}

function StatCard({ title, value, icon, accent }: StatCardProps) {
  return (
    <Card className={`border-l-4 ${accent}`}>
      <CardContent className="flex items-center justify-between p-5">
        <div>
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className="text-3xl font-bold mt-1">{value}</p>
        </div>
        <div className="text-muted-foreground opacity-60">{icon}</div>
      </CardContent>
    </Card>
  );
}

function SkeletonCard() {
  return (
    <Card className="border-l-4 border-l-muted">
      <CardContent className="p-5">
        <div className="h-4 w-24 bg-muted rounded animate-pulse mb-2" />
        <div className="h-8 w-12 bg-muted rounded animate-pulse" />
      </CardContent>
    </Card>
  );
}

interface StatsCardsProps {
  stats: StaffStats | null;
  loading: boolean;
}

/**
 * Displays four summary cards. Receives data as props — no fetching here.
 * The parent (App) owns the data and passes it down.
 */
export function StatsCards({ stats, loading }: StatsCardsProps) {
  if (loading || !stats) {
    return (
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
        title="Total Staff"
        value={`${stats.total} / ${stats.limit}`}
        icon={<Users size={32} />}
        accent="border-l-violet-500"
      />
      <StatCard
        title="Doctors"
        value={stats.doctors}
        icon={<Stethoscope size={32} />}
        accent="border-l-blue-500"
      />
      <StatCard
        title="Receptionists"
        value={stats.receptionists}
        icon={<MonitorCheck size={32} />}
        accent="border-l-emerald-500"
      />
      <StatCard
        title="Remaining Places"
        value={stats.remainingCapacity}
        icon={<AlertCircle size={32} />}
        accent={stats.remainingCapacity === 0 ? "border-l-red-500" : "border-l-amber-500"}
      />
    </div>
  );
}
