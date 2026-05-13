import { useState } from "react";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle,
  DialogDescription, DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { deleteStaff } from "@/api/staffApi";
import type { StaffMember } from "@/types/staff";

interface DeleteConfirmDialogProps {
  open: boolean;
  onClose: () => void;
  onMutate: () => void;   // tells App to re-fetch after deletion
  target: StaffMember | null;
}

export function DeleteConfirmDialog({ open, onClose, onMutate, target }: DeleteConfirmDialogProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const handleDelete = async () => {
    if (!target) return;
    setError(null);
    setLoading(true);
    try {
      await deleteStaff(target.staffId);
      onMutate();   // re-fetch in App
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) onClose(); }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Remove staff member?</DialogTitle>
          <DialogDescription>
            This will permanently remove{" "}
            <span className="font-semibold text-foreground">
              {target?.name} {target?.surname}
            </span>{" "}
            ({target?.staffId}). This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button variant="destructive" onClick={handleDelete} disabled={loading}>
            {loading ? "Removing…" : "Remove"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
