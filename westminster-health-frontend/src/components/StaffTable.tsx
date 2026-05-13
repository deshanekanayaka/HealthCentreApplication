import { useState } from "react";
import { Search, Plus, Pencil, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { StaffDialog } from "@/components/StaffDialog";
import { DeleteConfirmDialog } from "@/components/DeleteConfirmDialog";
import type { StaffMember } from "@/types/staff";

function formatDate(iso: string | null) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-GB", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function RoleBadge({ type }: { type: StaffMember["type"] }) {
  return (
    <Badge variant={type === "DOCTOR" ? "doctor" : "receptionist"}>
      {type === "DOCTOR" ? "Doctor" : "Receptionist"}
    </Badge>
  );
}

function TableSkeleton() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="h-14 rounded-md bg-muted animate-pulse" />
      ))}
    </div>
  );
}

function EmptyState({ hasSearch }: { hasSearch: boolean }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-muted-foreground">
      <Search size={40} className="mb-3 opacity-30" />
      <p className="font-medium">
        {hasSearch ? "No staff match your search" : "No staff members yet"}
      </p>
      <p className="text-sm mt-1">
        {hasSearch ? "Try a different name or ID" : "Add a doctor or receptionist to get started"}
      </p>
    </div>
  );
}

interface StaffTableProps {
  staff: StaffMember[];
  loading: boolean;
  error: string | null;
  onMutate: () => void;
}

/**
 * Displays the staff list. Receives data as props and calls onMutate()
 * after any successful add, edit, or delete — which triggers a fresh
 * fetch in the parent, keeping the whole UI in sync.
 *
 * Client-side search filters the already-fetched list — no extra
 * network requests needed for a dataset this size.
 */
export function StaffTable({ staff, loading, error, onMutate }: StaffTableProps) {
  const [search, setSearch]         = useState("");
  const [addDialogOpen, setAddDialog] = useState(false);
  const [editTarget, setEditTarget] = useState<StaffMember | null>(null);
  const [deleteTarget, setDelete]   = useState<StaffMember | null>(null);

  // Client-side filter — avoids a network round-trip for every keystroke
  const filtered = staff.filter((m) => {
    const q = search.toLowerCase();
    return (
      m.name.toLowerCase().includes(q) ||
      m.surname.toLowerCase().includes(q) ||
      m.staffId.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-4">
      {/* ── Toolbar ── */}
      <div className="flex gap-3 flex-col sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={16} />
          <Input
            placeholder="Search by name, surname or staff ID…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>
        <Button onClick={() => setAddDialog(true)}>
          <Plus size={16} className="mr-2" />
          Add Staff
        </Button>
      </div>

      {/* ── Content ── */}
      {loading ? (
        <TableSkeleton />
      ) : error ? (
        <p className="text-center text-destructive py-12">
          {error} — is the API running?
        </p>
      ) : filtered.length === 0 ? (
        <EmptyState hasSearch={search.length > 0} />
      ) : (
        <div className="rounded-lg border overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/50">
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Role</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Staff ID</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Name</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Date of Birth</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Phone</th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">Details</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map((member, idx) => (
                <tr
                  key={member.staffId}
                  className={`border-b last:border-0 hover:bg-muted/30 transition-colors ${
                    idx % 2 === 0 ? "" : "bg-muted/10"
                  }`}
                >
                  <td className="px-4 py-3"><RoleBadge type={member.type} /></td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{member.staffId}</td>
                  <td className="px-4 py-3 font-medium">{member.name} {member.surname}</td>
                  <td className="px-4 py-3 text-muted-foreground">{formatDate(member.dob)}</td>
                  <td className="px-4 py-3 text-muted-foreground">{member.phoneNo ?? "—"}</td>
                  <td className="px-4 py-3 text-muted-foreground text-xs">
                    {member.type === "DOCTOR" ? (
                      <>
                        {member.specialisation ?? "—"}
                        {member.consultationsPerWeek != null && (
                          <span className="ml-2 opacity-60">· {member.consultationsPerWeek} consult/wk</span>
                        )}
                      </>
                    ) : (
                      <>
                        Desk {member.deskNumber ?? "—"}
                        {member.hoursPerWeek != null && (
                          <span className="ml-2 opacity-60">· {member.hoursPerWeek} hrs/wk</span>
                        )}
                      </>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-1 justify-end">
                      <Button
                        variant="ghost" size="icon"
                        onClick={() => setEditTarget(member)}
                        aria-label={`Edit ${member.name} ${member.surname}`}
                      >
                        <Pencil size={15} />
                      </Button>
                      <Button
                        variant="ghost" size="icon"
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                        onClick={() => setDelete(member)}
                        aria-label={`Remove ${member.name} ${member.surname}`}
                      >
                        <Trash2 size={15} />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Dialogs ── */}
      <StaffDialog
        open={addDialogOpen}
        onClose={() => setAddDialog(false)}
        onMutate={onMutate}
      />
      <StaffDialog
        open={!!editTarget}
        onClose={() => setEditTarget(null)}
        editTarget={editTarget}
        onMutate={onMutate}
      />
      <DeleteConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDelete(null)}
        target={deleteTarget}
        onMutate={onMutate}
      />
    </div>
  );
}
