import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '@/views/RegisterView.vue';
import { useAuthStore } from '@/stores/auth.store';
import EmployeeView from '@/views/EmployeeView.vue';
import AtmView from '@/views/AtmView.vue';
import UserOverview from '@/views/EmployeeUserOverview.vue';

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'home',
        component: HomeView,
        meta: { guestOnly: true }
    },
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: { guestOnly: true }
    },
    {
        path: '/register',
        name: 'register',
        component: RegisterView,
        meta: { guestOnly: true }
    },
    {
        path: '/dashboard',
        name: 'dashboard',
        // Lazy-loading the dashboard component
        component: () => import('../views/DashboardView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/atm',
        name: 'atm',
        component: AtmView,
        meta: { requiresAuth: true }
    },
    {
        path: '/employeePanel',
        name: 'employeePanel',
        component: EmployeeView,
        meta: { requiresAuth: true }
    },
    {
        path: '/employeePanel/user/:id',
        name: 'userOverview',
        component: UserOverview,
        meta: { requiresAuth: true }
    },
    {
        // Catch-all redirect to home
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

// Navigation guard to check for authentication
router.beforeEach(async (to, from, next) => {
    // Check if the route requires authentication
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    // Check if the route is for guests only (login, register)
    const guestOnly = to.matched.some(record => record.meta.guestOnly);

    // Get auth store
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isLoggedIn;

    // Logic for authenticated routes
    if (requiresAuth) {
        if (isAuthenticated) {
            // Validate token on sensitive routes
            try {
                await authStore.validateToken();
                next();
            } catch (error) {
                next({ name: 'login' });
            }
        } else {
            // Redirect to login
            next({
                name: 'login',
                query: { redirect: to.fullPath }  // Save the route they were trying to access
            });
        }
    }    // Logic for guest-only routes
    else if (guestOnly && isAuthenticated) {
        // Redirect authenticated users accessing guest-only routes to dashboard
        next({ name: 'dashboard' });
    }
    // Allow access to public routes
    else {
        next();
    }
});

export default router;