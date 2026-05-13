import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import {
  createDoctor, createReceptionist,
  updateDoctor, updateReceptionist,
} from "@/api/staffApi";
import type { StaffMember } from "@/types/staff";

// ---- Zod schemas ------------------------------------------------------------

const shared = {
  name:    z.string().min(1, "Required").max(100),
  surname: z.string().min(1, "Required").max(100),
  staffId: z.string().regex(/^[A-Z]{2}\d{4}$/, "Format: 2 letters + 4 digits, e.g. DR0001"),
  dob:     z.string().optional(),
  phoneNo: z.string().optional(),
};

const doctorSchema = z.object({
  ...shared,
  licenceNumber:        z.string().regex(/^[A-Z0-9]{6,12}$/, "6–12 uppercase alphanumeric chars").or(z.literal("")).optional(),
  specialisation:       z.string().max(100).optional(),
  consultationsPerWeek: z.coerce.number().min(0).max(200).default(0),
});

const receptionistSchema = z.object({
  ...shared,
  deskNumber:   z.coerce.number().min(1, "Must be at least 1"),
  hoursPerWeek: z.coerce.number().min(1).max(80, "Max 80 hours"),
});

type DoctorForm       = z.infer<typeof doctorSchema>;
type ReceptionistForm = z.infer<typeof receptionistSchema>;
type Role = "DOCTOR" | "RECEPTIONIST";

// ---- Component --------------------------------------------------------------

interface StaffDialogProps {
  open: boolean;
  onClose: () => void;
  onMutate: () => void;        // ← tells the parent to re-fetch after success
  editTarget?: StaffMember | null;
}

export function StaffDialog({ open, onClose, onMutate, editTarget }: StaffDialogProps) {
  const isEditing = !!editTarget;
  const [role, setRole]           = useState<Role>(editTarget?.type ?? "DOCTOR");
  const [serverError, setError]   = useState<string | null>(null);
  const [saving, setSaving]       = useState(false);

  useEffect(() => {
    if (editTarget) setRole(editTarget.type);
  }, [editTarget]);

  const doctorForm = useForm<DoctorForm>({
    resolver: zodResolver(doctorSchema),
    defaultValues: {
      name: "", surname: "", staffId: "", dob: "", phoneNo: "",
      licenceNumber: "", specialisation: "", consultationsPerWeek: 0,
    },
  });

  const receptionistForm = useForm<ReceptionistForm>({
    resolver: zodResolver(receptionistSchema),
    defaultValues: {
      name: "", surname: "", staffId: "", dob: "", phoneNo: "",
      deskNumber: 1, hoursPerWeek: 37,
    },
  });

  useEffect(() => {
    if (!editTarget) return;
    if (editTarget.type === "DOCTOR") {
      doctorForm.reset({
        name: editTarget.name, surname: editTarget.surname,
        staffId: editTarget.staffId, dob: editTarget.dob ?? "",
        phoneNo: editTarget.phoneNo ?? "",
        licenceNumber: editTarget.licenceNumber ?? "",
        specialisation: editTarget.specialisation ?? "",
        consultationsPerWeek: editTarget.consultationsPerWeek ?? 0,
      });
    } else {
      receptionistForm.reset({
        name: editTarget.name, surname: editTarget.surname,
        staffId: editTarget.staffId, dob: editTarget.dob ?? "",
        phoneNo: editTarget.phoneNo ?? "",
        deskNumber: editTarget.deskNumber ?? 1,
        hoursPerWeek: editTarget.hoursPerWeek ?? 37,
      });
    }
  }, [editTarget, doctorForm, receptionistForm]);

  const handleClose = () => {
    doctorForm.reset();
    receptionistForm.reset();
    setError(null);
    onClose();
  };

  const onDoctorSubmit = async (values: DoctorForm) => {
    setError(null);
    setSaving(true);
    try {
      const payload = {
        name: values.name, surname: values.surname,
        staffId: values.staffId, dob: values.dob || null,
        phoneNo: values.phoneNo || null,
        licenceNumber: values.licenceNumber || null,
        specialisation: values.specialisation || null,
        consultationsPerWeek: values.consultationsPerWeek,
      };
      if (isEditing) {
        await updateDoctor(editTarget!.staffId, payload);
      } else {
        await createDoctor(payload);
      }
      onMutate();   // tell App to re-fetch
      handleClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong.");
    } finally {
      setSaving(false);
    }
  };

  const onReceptionistSubmit = async (values: ReceptionistForm) => {
    setError(null);
    setSaving(true);
    try {
      const payload = {
        name: values.name, surname: values.surname,
        staffId: values.staffId, dob: values.dob || null,
        phoneNo: values.phoneNo || null,
        deskNumber: values.deskNumber,
        hoursPerWeek: values.hoursPerWeek,
      };
      if (isEditing) {
        await updateReceptionist(editTarget!.staffId, payload);
      } else {
        await createReceptionist(payload);
      }
      onMutate();   // tell App to re-fetch
      handleClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) handleClose(); }}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Edit Staff Member" : "Add Staff Member"}</DialogTitle>
        </DialogHeader>

        <div className="space-y-1">
          <Label>Role</Label>
          <Select value={role} onValueChange={(v) => setRole(v as Role)} disabled={isEditing}>
            <SelectTrigger><SelectValue /></SelectTrigger>
            <SelectContent>
              <SelectItem value="DOCTOR">Doctor</SelectItem>
              <SelectItem value="RECEPTIONIST">Receptionist</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {role === "DOCTOR"
          ? <DoctorFields form={doctorForm} isEditing={isEditing} onSubmit={onDoctorSubmit} saving={saving} serverError={serverError} onClose={handleClose} />
          : <ReceptionistFields form={receptionistForm} isEditing={isEditing} onSubmit={onReceptionistSubmit} saving={saving} serverError={serverError} onClose={handleClose} />
        }
      </DialogContent>
    </Dialog>
  );
}

// ---- Helpers ----------------------------------------------------------------

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs text-destructive mt-1">{message}</p>;
}

function SharedFields({ form, isEditing }: { form: ReturnType<typeof useForm<DoctorForm>>; isEditing: boolean }) {
  const { register, formState: { errors } } = form;
  return (
    <div className="grid grid-cols-2 gap-3">
      <div>
        <Label>First name *</Label>
        <Input {...register("name")} />
        <FieldError message={errors.name?.message} />
      </div>
      <div>
        <Label>Surname *</Label>
        <Input {...register("surname")} />
        <FieldError message={errors.surname?.message} />
      </div>
      <div>
        <Label>Staff ID *</Label>
        <Input {...register("staffId")} disabled={isEditing} placeholder="DR0001" className="uppercase" />
        <FieldError message={errors.staffId?.message} />
      </div>
      <div>
        <Label>Date of birth</Label>
        <Input {...register("dob")} type="date" />
      </div>
      <div className="col-span-2">
        <Label>Phone number</Label>
        <Input {...register("phoneNo")} placeholder="+44 7700 900000" />
      </div>
    </div>
  );
}

function DoctorFields({ form, isEditing, onSubmit, saving, serverError, onClose }:
  { form: ReturnType<typeof useForm<DoctorForm>>; isEditing: boolean; onSubmit: (v: DoctorForm) => void; saving: boolean; serverError: string | null; onClose: () => void }) {
  const { register, formState: { errors }, handleSubmit } = form;
  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <SharedFields form={form} isEditing={isEditing} />
      <div className="border-t pt-3 space-y-3">
        <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Doctor details</p>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <Label>Licence number</Label>
            <Input {...register("licenceNumber")} placeholder="GMC123456" className="uppercase" />
            <FieldError message={errors.licenceNumber?.message} />
          </div>
          <div>
            <Label>Specialisation</Label>
            <Input {...register("specialisation")} placeholder="Cardiology" />
          </div>
          <div>
            <Label>Consultations / week</Label>
            <Input {...register("consultationsPerWeek")} type="number" min={0} max={200} />
            <FieldError message={errors.consultationsPerWeek?.message} />
          </div>
        </div>
      </div>
      {serverError && <p className="text-sm text-destructive bg-destructive/10 rounded p-2">{serverError}</p>}
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
        <Button type="submit" disabled={saving}>{saving ? "Saving…" : "Save"}</Button>
      </DialogFooter>
    </form>
  );
}

function ReceptionistFields({ form, isEditing, onSubmit, saving, serverError, onClose }:
  { form: ReturnType<typeof useForm<ReceptionistForm>>; isEditing: boolean; onSubmit: (v: ReceptionistForm) => void; saving: boolean; serverError: string | null; onClose: () => void }) {
  const { register, formState: { errors }, handleSubmit } = form;
  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <SharedFields form={form as never} isEditing={isEditing} />
      <div className="border-t pt-3 space-y-3">
        <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Receptionist details</p>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <Label>Desk number</Label>
            <Input {...register("deskNumber")} type="number" min={1} />
            <FieldError message={errors.deskNumber?.message} />
          </div>
          <div>
            <Label>Hours / week</Label>
            <Input {...register("hoursPerWeek")} type="number" min={1} max={80} />
            <FieldError message={errors.hoursPerWeek?.message} />
          </div>
        </div>
      </div>
      {serverError && <p className="text-sm text-destructive bg-destructive/10 rounded p-2">{serverError}</p>}
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>Cancel</Button>
        <Button type="submit" disabled={saving}>{saving ? "Saving…" : "Save"}</Button>
      </DialogFooter>
    </form>
  );
}
