import { createRouter, createWebHistory } from "vue-router";

// Pages
import PostList from "../pages/PostList.vue";
import PostPage from "../pages/PostPage.vue";
import CreatePost from "../pages/CreatePost.vue";
import Login from "../pages/Login.vue";
import CreateUser from "../pages/CreateUser.vue";

// ----------------------
// AUTH HELPERS
// ----------------------

function getToken() {
  return localStorage.getItem("token");
}

function isLoggedIn() {
  return !!getToken();
}

// centralized logout (IMPORTANT)
export function logout(router) {
  localStorage.removeItem("token");
  router.replace("/");
}

// ----------------------
// ROUTES
// ----------------------

const routes = [
  {
    path: "/",
    name: "Home",
    component: PostList,
  },
  {
    path: "/post/:id",
    name: "Post",
    component: PostPage,
    props: true,
  },
  {
    path: "/create",
    name: "CreatePost",
    component: CreatePost,
    meta: { requiresAuth: true },
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/register",
    name: "CreateUser",
    component: CreateUser,
  },
];

// ----------------------
// ROUTER
// ----------------------

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// ----------------------
// GLOBAL GUARD
// ----------------------

router.beforeEach((to, from, next) => {
  const token = getToken();

  if (to.meta.requiresAuth && !token) {
    next("/login");
    return;
  }

  next();
});

export default router;