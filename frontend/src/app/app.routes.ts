import { Routes } from '@angular/router';

// Chỉ /admin và /staff là route của SPA. /q/{code} KHÔNG nằm ở đây — nó là trang Thymeleaf
// server-render, không phải phần của SPA (PLAN.md § 1.2).
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
