import { createRouter, createWebHistory } from "vue-router";

// Pages
import PostList from "../pages/PostList.vue";
import PostPage from "../pages/PostPage.vue";
import CreatePost from "../pages/CreatePost.vue";
import Login from "../pages/Login.vue";
import CreateUser from "../pages/CreateUser.vue"; // 👈 added

// simple auth check
function isLoggedIn() {
  return !!localStorage.getItem("token");
}

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

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 🔐 route guard
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    next("/login");
  } else {
    next();
  }
});

export default router;