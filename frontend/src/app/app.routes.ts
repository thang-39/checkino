import { Routes } from '@angular/router';

// Only /admin and /staff are SPA routes. /q/{code} is NOT here — it is a server-rendered
// Thymeleaf page, not part of the SPA (PLAN.md § 1.2).
export const routes: Routes = [
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin').then((m) => m.Admin),
  },
  {
    path: 'staff',
    loadComponent: () => import('./staff/staff').then((m) => m.Staff),
  },
  { path: '', pathMatch: 'full', redirectTo: 'admin' },
];
