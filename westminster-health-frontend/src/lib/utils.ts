import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Merges Tailwind classes safely, resolving conflicts.
 * Used by every shadcn/ui component internally.
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
