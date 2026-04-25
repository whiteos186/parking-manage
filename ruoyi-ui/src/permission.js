import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

/**
 * Walk the sidebar router tree to find the first leaf route path.
 * Used to redirect customer-role users away from pages they cannot access.
 */
function resolveFirstRoute(routes) {
  if (!routes || routes.length === 0) return null
  for (const route of routes) {
    if (route.hidden) continue
    if (route.children && route.children.length > 0) {
      const child = resolveFirstRoute(route.children)
      if (child) {
        if (child.startsWith('/')) return child
        const parentPath = route.path || ''
        return parentPath ? `/${parentPath}/${child}`.replace(/\/+/g, '/') : child
      }
    } else if (route.path) {
      return route.path
    }
  }
  return null
}

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    const isLock = store.getters.isLock
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else if (isLock && to.path !== '/lock') {
      next({ path: '/lock' })
      NProgress.done()
    } else if (!isLock && to.path === '/lock') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            router.addRoutes(accessRoutes) // 动态添加可访问路由表
            // Customer role does not have overview access; redirect to first available page
            if (to.path === '/' || to.path === '/parking') {
              const roles = store.getters.roles
              if (roles.includes('customer')) {
                const firstRoute = resolveFirstRoute(store.getters.sidebarRouters)
                if (firstRoute && firstRoute !== '/parking') {
                  next({ path: firstRoute, replace: true })
                  return
                }
              }
            }
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch(err => {
            store.dispatch('LogOut').then(() => {
              Message.error(err)
              next({ path: '/' })
            })
          })
      } else {
        // Customer role does not have overview access; redirect to first available page
        const roles = store.getters.roles
        if (roles.includes('customer') && (to.path === '/' || to.path === '/parking')) {
          const firstRoute = resolveFirstRoute(store.getters.sidebarRouters)
          if (firstRoute && firstRoute !== '/parking') {
            next({ path: firstRoute, replace: true })
            return
          }
        }
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
