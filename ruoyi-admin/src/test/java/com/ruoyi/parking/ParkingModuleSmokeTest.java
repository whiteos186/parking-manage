package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ParkingModuleSmokeTest
{
    @Test
    void parkingUiUsesChineseCopyAndCrudPermissionButtons() throws IOException
    {
        String overviewVue = readProjectFile("ruoyi-ui/src/views/parking/index.vue");
        assertTrue(overviewVue.contains("\u505c\u8f66\u573a\u603b\u6570"), "Expected overview page to be localized to Chinese");
        assertTrue(overviewVue.contains("\u8f66\u4f4d\u603b\u6570"), "Expected overview page to use Chinese statistics labels");
        assertTrue(overviewVue.contains("\u5237\u65b0"), "Expected overview action text to be Chinese");
        assertTrue(
            overviewVue.contains("v-hasPermi=\"['parking:overview:list']\""),
            "Expected overview refresh action to be guarded by parking:overview:list permission"
        );
        assertTrue(overviewVue.contains("/archives/lot"), "Expected overview lot quick action to use archive route");
        assertTrue(overviewVue.contains("/archives/space"), "Expected overview space quick action to use archive route");
        assertTrue(overviewVue.contains("/operations/temp"), "Expected overview temp quick action to use operations route");
        assertTrue(overviewVue.contains("/operations/monthly"), "Expected overview monthly quick action to use operations route");
        assertTrue(overviewVue.contains("/operations/membership"), "Expected overview membership quick action to use operations route");
        assertTrue(overviewVue.contains("/operations/payment"), "Expected overview payment quick action to use operations route");
        assertTrue(overviewVue.contains("/customers/customer"), "Expected overview customer quick action to use customers route");
        assertTrue(overviewVue.contains("/customers/vehicle"), "Expected overview vehicle quick action to use customers route");
        assertTrue(overviewVue.contains("/archives/lotadmin"), "Expected overview lot-admin quick action to use archive route");

        String routerJs = readProjectFile("ruoyi-ui/src/router/index.js");
        assertTrue(routerJs.contains("redirect: '/parking'"), "Expected root route to redirect to parking workbench");
        assertTrue(routerJs.contains("hidden: true"), "Expected root workbench shortcut route to stay hidden from sidebar");
        assertTrue(!routerJs.contains("title: '首页'"), "Expected router to stop exposing the generic 首页 label");

        String devEnv = readProjectFile("ruoyi-ui/.env.development");
        assertTrue(devEnv.contains("VUE_APP_TITLE = 停车场管理系统"), "Expected development title to use parking system branding");

        String prodEnv = readProjectFile("ruoyi-ui/.env.production");
        assertTrue(prodEnv.contains("VUE_APP_TITLE = 停车场管理系统"), "Expected production title to use parking system branding");

        String stagingEnv = readProjectFile("ruoyi-ui/.env.staging");
        assertTrue(stagingEnv.contains("VUE_APP_TITLE = 停车场管理系统"), "Expected staging title to use parking system branding");

        String settingsJs = readProjectFile("ruoyi-ui/src/settings.js");
        assertTrue(settingsJs.contains("停车场管理系统"), "Expected shared settings copy to use parking system branding");

        String navbarVue = readProjectFile("ruoyi-ui/src/layout/components/Navbar.vue");
        assertTrue(!navbarVue.contains("RuoYiGit"), "Expected navbar to stop importing the RuoYi source shortcut");
        assertTrue(!navbarVue.contains("RuoYiDoc"), "Expected navbar to stop importing the RuoYi doc shortcut");
        assertTrue(navbarVue.contains("location.href = '/parking'"), "Expected logout to return to the parking workbench");

        String breadcrumbVue = readProjectFile("ruoyi-ui/src/components/Breadcrumb/index.vue");
        assertTrue(breadcrumbVue.contains("path: \"/parking\""), "Expected breadcrumb root to point at parking workbench");
        assertTrue(breadcrumbVue.contains("title: \"工作台\""), "Expected breadcrumb root title to use 工作台");

        String packageJson = readProjectFile("ruoyi-ui/package.json");
        assertTrue(packageJson.contains("停车场管理系统"), "Expected package metadata to use parking system branding");

        String lotVue = readProjectFile("ruoyi-ui/src/views/parking/lot/index.vue");
        assertTrue(lotVue.contains("\u505c\u8f66\u573a\u7ba1\u7406"), "Expected lot management page to use Chinese title");
        assertTrue(lotVue.contains("v-hasPermi=\"['parking:lot:add']\""), "Expected lot add permission button");
        assertTrue(lotVue.contains("v-hasPermi=\"['parking:lot:edit']\""), "Expected lot edit permission button");
        assertTrue(lotVue.contains("v-hasPermi=\"['parking:lot:remove']\""), "Expected lot remove permission button");

        String spaceVue = readProjectFile("ruoyi-ui/src/views/parking/space/index.vue");
        assertTrue(spaceVue.contains("\u8f66\u4f4d\u7ba1\u7406"), "Expected space management page to use Chinese title");
        assertTrue(spaceVue.contains("v-hasPermi=\"['parking:space:add']\""), "Expected space add permission button");
        assertTrue(spaceVue.contains("v-hasPermi=\"['parking:space:edit']\""), "Expected space edit permission button");
        assertTrue(spaceVue.contains("v-hasPermi=\"['parking:space:remove']\""), "Expected space remove permission button");

        String tempVue = readProjectFile("ruoyi-ui/src/views/parking/temp/index.vue");
        assertTrue(tempVue.contains("\u4e34\u505c\u8ba2\u5355"), "Expected temp order page to use Chinese title");
        assertTrue(tempVue.contains("v-hasPermi=\"['parking:temp:add']\""), "Expected temp add permission button");
        assertTrue(tempVue.contains("v-hasPermi=\"['parking:temp:edit']\""), "Expected temp edit permission button");
        assertTrue(tempVue.contains("v-hasPermi=\"['parking:temp:remove']\""), "Expected temp remove permission button");
        assertTrue(tempVue.contains("v-hasPermi=\"['parking:temp:settle']\""), "Expected temp settle permission button");
        assertTrue(tempVue.contains("v-hasPermi=\"['parking:temp:entry']\""), "Expected temp entry permission button");
        assertTrue(tempVue.contains("v-hasPermi=\"['parking:temp:exit']\""), "Expected temp exit permission button");

        String monthlyVue = readProjectFile("ruoyi-ui/src/views/parking/monthly/index.vue");
        assertTrue(monthlyVue.contains("\u6708\u5361\u8ba2\u5355"), "Expected monthly order page to use Chinese title");
        assertTrue(monthlyVue.contains("v-hasPermi=\"['parking:monthly:add']\""), "Expected monthly add permission button");
        assertTrue(monthlyVue.contains("v-hasPermi=\"['parking:monthly:edit']\""), "Expected monthly edit permission button");
        assertTrue(monthlyVue.contains("v-hasPermi=\"['parking:monthly:remove']\""), "Expected monthly remove permission button");
        assertTrue(monthlyVue.contains("v-hasPermi=\"['parking:monthly:pay']\""), "Expected monthly pay permission button");
        assertTrue(monthlyVue.contains("v-hasPermi=\"['parking:monthly:cancel']\""), "Expected monthly cancel permission button");
        assertTrue(monthlyVue.contains("listVehicleOptions"), "Expected monthly order form to load parking vehicle options");
        assertTrue(monthlyVue.contains("vehicleOptions"), "Expected monthly order form to render vehicle options");
        assertTrue(!monthlyVue.contains("\u8f66\u8f86ID"), "Expected monthly order form to stop exposing raw vehicle ID copy");

        String membershipVue = readProjectFile("ruoyi-ui/src/views/parking/membership/index.vue");
        assertTrue(membershipVue.contains("\u4f1a\u5458\u8ba2\u5355"), "Expected membership order page to use Chinese title");
        assertTrue(membershipVue.contains("v-hasPermi=\"['parking:membership:add']\""), "Expected membership add permission button");
        assertTrue(membershipVue.contains("v-hasPermi=\"['parking:membership:edit']\""), "Expected membership edit permission button");
        assertTrue(membershipVue.contains("v-hasPermi=\"['parking:membership:remove']\""), "Expected membership remove permission button");
        assertTrue(membershipVue.contains("v-hasPermi=\"['parking:membership:pay']\""), "Expected membership pay permission button");
        assertTrue(membershipVue.contains("v-hasPermi=\"['parking:membership:cancel']\""), "Expected membership cancel permission button");
        assertTrue(membershipVue.contains("listVehicleOptions"), "Expected membership order form to load parking vehicle options");
        assertTrue(membershipVue.contains("vehicleOptions"), "Expected membership order form to render vehicle options");
        assertTrue(!membershipVue.contains("\u8f66\u8f86ID"), "Expected membership order form to stop exposing raw vehicle ID copy");

        String paymentVue = readProjectFile("ruoyi-ui/src/views/parking/payment/index.vue");
        assertTrue(paymentVue.contains("\u652f\u4ed8\u6d41\u6c34"), "Expected payment record page to use Chinese title");
        assertTrue(paymentVue.contains("v-hasPermi=\"['parking:payment:add']\""), "Expected payment add permission button");
        assertTrue(paymentVue.contains("v-hasPermi=\"['parking:payment:edit']\""), "Expected payment edit permission button");
        assertTrue(paymentVue.contains("v-hasPermi=\"['parking:payment:remove']\""), "Expected payment remove permission button");
        assertTrue(paymentVue.contains("v-hasPermi=\"['parking:payment:refund']\""), "Expected payment refund permission button");
        assertTrue(paymentVue.contains("customerId"), "Expected payment page to bind parking customer identity");
        assertTrue(!paymentVue.contains("userId"), "Expected payment page to stop exposing raw backend userId");

        String customerVue = readProjectFile("ruoyi-ui/src/views/parking/customer/index.vue");
        assertTrue(customerVue.contains("\u5ba2\u6237\u6863\u6848"), "Expected customer archive page to use Chinese title");
        assertTrue(customerVue.contains("v-hasPermi=\"['parking:customer:add']\""), "Expected customer add permission button");
        assertTrue(customerVue.contains("v-hasPermi=\"['parking:customer:edit']\""), "Expected customer edit permission button");
        assertTrue(customerVue.contains("v-hasPermi=\"['parking:customer:remove']\""), "Expected customer remove permission button");

        String vehicleVue = readProjectFile("ruoyi-ui/src/views/parking/vehicle/index.vue");
        assertTrue(vehicleVue.contains("\u7528\u6237\u8f66\u8f86"), "Expected user vehicle page to use Chinese title");
        assertTrue(vehicleVue.contains("v-hasPermi=\"['parking:vehicle:add']\""), "Expected vehicle add permission button");
        assertTrue(vehicleVue.contains("v-hasPermi=\"['parking:vehicle:edit']\""), "Expected vehicle edit permission button");
        assertTrue(vehicleVue.contains("v-hasPermi=\"['parking:vehicle:remove']\""), "Expected vehicle remove permission button");
        assertTrue(vehicleVue.contains("v-hasPermi=\"['parking:vehicle:setDefault']\""), "Expected vehicle setDefault permission button");
        assertTrue(vehicleVue.contains("customerId"), "Expected vehicle page to bind parking customer identity");
        assertTrue(!vehicleVue.contains("userId"), "Expected vehicle page to stop exposing raw backend userId");

        String lotAdminVue = readProjectFile("ruoyi-ui/src/views/parking/lotadmin/index.vue");
        assertTrue(lotAdminVue.contains("\u7ba1\u7406\u5458\u7ed1\u5b9a"), "Expected lot admin page to use Chinese title");
        assertTrue(lotAdminVue.contains("v-hasPermi=\"['parking:lotadmin:add']\""), "Expected lotadmin add permission button");
        assertTrue(lotAdminVue.contains("v-hasPermi=\"['parking:lotadmin:edit']\""), "Expected lotadmin edit permission button");
        assertTrue(lotAdminVue.contains("v-hasPermi=\"['parking:lotadmin:remove']\""), "Expected lotadmin remove permission button");
        assertTrue(lotAdminVue.contains("userId"), "Expected lot-admin page to remain on internal user bindings");
    }

    private static String readProjectFile(String relativePath) throws IOException
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(relativePath);
            if (Files.exists(candidate))
            {
                return Files.readString(candidate, StandardCharsets.UTF_8);
            }
            current = current.getParent();
        }
        throw new IOException("Missing file: " + relativePath);
    }
}
