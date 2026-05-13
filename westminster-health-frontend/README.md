# Westminster Health Centre — Frontend

React frontend for the Westminster Health Centre Staff Manager.
Connects to the Spring Boot backend API.

**Live app:** `https://your-app.vercel.app`
**Backend API:** `https://your-api.railway.app`

---

## Tech stack

| Layer | Technology |
|---|---|
| Framework | React 18 + TypeScript |
| Build tool | Vite |
| Styling | Tailwind CSS + shadcn/ui |
| Data fetching | TanStack Query v5 |
| Forms | React Hook Form + Zod |
| Icons | Lucide React |

---

## Running locally

### Prerequisites
- Node.js 18+
- The backend API running on `http://localhost:8080`

### Setup
```bash
cd frontend
npm install

# Create your local env file
cp .env.example .env.local
# .env.local already points to http://localhost:8080 by default

npm run dev
```

Open `http://localhost:5173`.

---

## Key decisions

### TanStack Query for data fetching
TanStack Query manages loading states, error states, caching, and automatic background refetches. After any mutation (add/edit/delete) it invalidates the relevant queries so the UI stays in sync with the server without a manual refresh.

### React Hook Form + Zod
Forms use React Hook Form for performance (uncontrolled inputs, no re-renders on every keystroke) and Zod for schema validation. The Zod schemas mirror the backend's Bean Validation annotations, so validation is consistent on both sides.

### Single API module
All `fetch` calls live in `src/api/staffApi.ts`. Centralising them makes the base URL easy to change, and error handling happens in one place (`handleResponse`).

### TypeScript types mirror backend DTOs
`src/types/staff.ts` defines interfaces that exactly match the backend's Java records. If the backend contract changes, TypeScript will flag every affected callsite in the frontend.

---

## Deploying to Vercel

1. Push this folder to GitHub.
2. Go to [vercel.com](https://vercel.com) → **Add New Project** → import the repo.
3. Set the **Root Directory** to `frontend`.
4. Add an environment variable:
   ```
   VITE_API_URL = https://your-api.railway.app
   ```
5. Deploy. Vercel detects Vite automatically.

---

## Project structure

```
src/
├── api/
│   └── staffApi.ts          All fetch calls + typed error handling
├── types/
│   └── staff.ts             TypeScript interfaces mirroring backend DTOs
├── hooks/
│   └── useStaff.ts          TanStack Query hooks (queries + mutations)
├── components/
│   ├── ui/                  shadcn/ui base components
│   ├── StatsCards.tsx       4-card summary row
│   ├── StaffTable.tsx       Main table with search, edit, delete
│   ├── StaffDialog.tsx      Add/edit modal (React Hook Form + Zod)
│   └── DeleteConfirmDialog.tsx
├── lib/
│   └── utils.ts             cn() helper for Tailwind class merging
├── App.tsx                  Root layout + QueryClientProvider
└── main.tsx                 React entry point
```
